'use strict';

angular.module('emr.ui.grids', [])

    .directive('timeSeriesGrid', ['$window', function($window) {

        return {

            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/grids/timeSeriesGrid.html',
            scope: {

                chartData: "="
            },
            link: function ($scope, element, attrs) {

                var windowSize = 50;
                var raw = element[0];

                $scope.showing = [];
                $scope.columnNames = [];
                $scope.currentIndex = 0;
                $scope.gridWidth = 0;

                element.bind('scroll', function() {

                    if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight - 1) {

                        $scope.$apply(loadMoreData());
                    }
                });

                $scope.$watch("chartData", function () {

                    prepareData();
                });

                var loadMoreData = function() {

                    loadDataByRows();

                    $scope.currentIndex = $scope.currentIndex + windowSize;
                };

                var prepareData = function() {

                    if (!$scope.chartData) return;

                    $scope.showing = [];
                    $scope.currentIndex = 0;

                    var columnNames = ['Index'];
                    columnNames = columnNames.concat($scope.chartData[0]);
                    $scope.showing.push(columnNames);

                    loadDataByRows();

                    $scope.currentIndex = windowSize;

                    $scope.gridWidth = calculateGridWidth(columnNames.length);
                };

                var calculateGridWidth = function(numberOfColumns) {

                    var width = 0;

                    if (numberOfColumns < 5) {

                        width = 207 * numberOfColumns;
                    }
                    else if (numberOfColumns < 10) {

                        width = 190 * numberOfColumns;
                    }
                    else {

                        width = 185 * numberOfColumns;
                    }

                    return width;
                };


                var formatUnixTime = function(item) {

                    var date = new Date(item * 1000);

                    var validDate = !isNaN(date.valueOf());

                    if (!validDate) {

                        return item;
                    }

                    return date.toISOString().replace('T', ' ').replace('Z', '');
                };

                var formatNumber = function(item) {

                    if (isNaN(item)) {

                        return 'NaN';
                    }

                    return item.toFixed(5);
                };

                var loadDataByRows = function() {

                    var stoppingPoint = $scope.currentIndex + windowSize;

                    var numberOfObservations = $scope.chartData[1].length;

                    if (stoppingPoint >= numberOfObservations) {

                        stoppingPoint = numberOfObservations - 1;
                    }

                    for (var rowIndex = $scope.currentIndex; rowIndex <= stoppingPoint; rowIndex++) {

                        var row = [];

                        for (var column = 1; column < $scope.chartData.length; column++) {

                            if (column == 1) {

                                row.push(formatUnixTime($scope.chartData[column][rowIndex]));
                            }
                            else {

                                row.push(formatNumber($scope.chartData[column][rowIndex]));
                            }
                        }

                        $scope.showing.push(row);
                    }

                };

                prepareData();
            }
        }
    }])

    .directive('featuresGrid', ['$window', function($window){

        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/assets/scripts/components/grids/featuresGrid.html',
            scope: {
                features: "=",
                columnWidth: "=",
                rowHeaderWidth: "="
            },
            link: function ($scope, element, attrs) {

                // initialize position variables
                $scope.padding = 10;
                $scope.columnHeight = 36;
                $scope.columnHeaderPosition = 0;
                $scope.rowHeaderPosition = 0;
                $scope.loadedRecordCount = 200;
                $scope.originX = $scope.rowHeaderWidth + 2 * $scope.padding;
                $scope.originY = $scope.columnHeight + 2 * $scope.padding;

                var featureCount = Object.keys($scope.features).length;
                $scope.gridWidth = (featureCount * ($scope.columnWidth + $scope.padding) + $scope.originX);
                $scope.gridHeight = ($scope.loadedRecordCount * ($scope.columnHeight + $scope.padding) + $scope.originY);


                angular.element('#grid-content-container').bind('scroll', function(event) {

                    $scope.$apply(function(){
                        $scope.columnHeaderPosition = -1 * event.target.scrollLeft;
                        $scope.rowHeaderPosition = -1 * event.target.scrollTop;
                    });

                });
            }
        }
    }]);