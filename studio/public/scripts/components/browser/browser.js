'use strict';

angular.module('browserApp', ['ngAnimate', 'draggableApp'])
    .directive('browser', ['$compile', function ($compile) {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/browser/browser.html',
            scope: {
                nodes: "=",
                onDrop: "=",
                getConfigBlock: "=",
                loadSources: "=",
                savable: "=",
                output: "="
            },
            link: function($scope, element, attrs){

                $scope.pageIndex = 0;

                $scope.pageHeader = [];

                $scope.pages = [{
                    "categories" : $scope.nodes,
                    "definitions" : [],
                    "definition" : null
                }];

                //
                // The definition is only configurable if a load-definition function has
                // been specified.
                //
                $scope.isConfigurable = function(){

                    if ($scope.onDrop !== undefined)
                        return false;
                    else
                        return true;
                };

                // fire the event to begin dragging an element
                var beginDragEvent = function(x, y, config){

                    $scope.$root.$broadcast("beginDrag", {
                        x: x,
                        y: y,
                        config: config
                    });
                };

                // set page index
                $scope.setPage = function(index){
                    $scope.pageIndex = index;
                };

                // identify whether index is current page
                $scope.isCurrentPage = function(index){
                    return $scope.pageIndex === index;
                };

                // page the navigation panel back
                $scope.pageBack = function(){

                    $scope.pages.splice($scope.pageIndex);
                    $scope.pageIndex = $scope.pageIndex - 1;
                    $scope.pageHeader.splice($scope.pageIndex);
                    $scope.savable = false;
                };

                // page the navigation panel forward
                $scope.pageForward = function(item){

                    $scope.pageHeader.push(item.name);

                    var index = $scope.pageIndex + 1;
                    $scope.pages.push({
                            "categories" : item.categories,
                            "definitions" : item.definitions,
                            "definition" : null
                        }
                    );

                    $scope.pageIndex = index;

                    $scope.savable = false;
                };

                // display a definition for configuration
                $scope.show = function(item){

                    if($scope.isConfigurable()) {

                        $scope.pageHeader.push(item.name);

                        var index = $scope.pageIndex + 1;

                        $scope.output = $scope.getConfigBlock(0, 0, item.name);

                        $scope.pages.push({
                                "categories": [],
                                "definitions": [],
                                "definition": $scope.output
                            });

                        $scope.pageIndex = index;

                        $scope.savable = true;
                    }
                };

                $scope.mouseDown = function(evt, item){

                    if($scope.onDrop) {

                        beginDragEvent(evt.pageX, evt.pageY, {

                            dragStarted: function (x, y) {

                                // create the element that represents the dragging definition
                                var ghost = angular.element('<div id="ghost" ' +
                                    'ng-style="{top: ghostPosition.y, ' +
                                    'left: ghostPosition.x, ' +
                                    'width: 200, ' +
                                    'height: 80}"></div>');

                                // compile and append to the parent
                                $compile(ghost)($scope);
                                element.parent().append(ghost);
                            },

                            dragging: function (x, y) {

                                // update the ghost position
                                $scope.ghostPosition = {x: x, y: y};
                            },

                            dragEnded: function (x, y, evt) {

                                $scope.onDrop(x, y, evt, item.name);

                                delete $scope.ghostPosition;

                                $scope.$apply(function () {
                                    angular.element("#ghost").remove();
                                });
                            }
                        });

                        evt.stopPropagation();
                        evt.preventDefault();
                    }
                };
            }
        }
    }])
    .directive('blockConfig', [function () {

        return {
            restrict: 'E',
            replace: false,
            templateUrl: '/assets/scripts/components/browser/blockConfig.html',
            scope: {
                block: "=",
                loadSources: "="
            },
            link: function ($scope, element, attrs) {

                $scope.users = [
                    { "id": 1, "name": "Ali" },
                    { "id": 2, "name": "Sara" },
                    { "id": 3, "name": "Babak" },
                    { "id": 4, "name": "Sanaz" },
                    { "id": 5, "name": "Dariush" },
                ];

                $scope.selectedUserIds = ["Sara"];



                // list of dependants for reverse lookup
                $scope.dependants = {};

                var init = function(){

                    // assemble initial list of dynamic parameters
                    var dynamicSourceRequests = {
                        name: $scope.block.name,
                        parameters: [],
                        diagram: null
                    };
                    $scope.block.parameters.forEach(function(parameter){

                        if (parameter.source()) {

                            // build a list of dependencies and track whether dependencies have
                            // been collected
                            var dependenciesCollected = true;

                            parameter.source().arguments.forEach(function (argument) {

                                if (argument.type === 0) {
                                    // represents a parameter dependency

                                    var dependencyName = argument.name;

                                    if (!parameter.loaded) {

                                        var dependency = getParameter(dependencyName);
                                        if (dependency.collected) {
                                            argument.value = dependency.value;
                                        }
                                        else {
                                            dependenciesCollected = false;
                                        }
                                    }

                                    if (!(dependencyName in $scope.dependants))
                                        $scope.dependants[dependencyName] = [];

                                    $scope.dependants[dependencyName].push(parameter.name());
                                }
                                else if (argument.type === 1) { // represents a block name

                                    argument.value = $scope.block.name;
                                }
                            });

                            // if the parameter is not loaded and has no dependencies -
                            // flag as loading and add to the dynamicSourceRequests list

                            if (!parameter.loaded && dependenciesCollected) {
                                parameter.loading = true;
                                dynamicSourceRequests.parameters.push(parameter.data);
                            }
                        }
                    });

                    loadDynamicSources(dynamicSourceRequests);
                };

                var loadDynamicSources = function(request){

                    if (request.parameters.length === 0)
                        return;

                    $scope.loadSources(request, function(response){

                        response.parameters.forEach(function(source) {

                            var parameter = getParameter(source.name);

                            // toggle value - todo: determine whether this is still necessary
                            var temp = parameter.value;
                            parameter.value = "";

                            if (parameter.data.type == 'multiSelectList') {
                                var idIndex = 0;
                                var options = [];
                                source.fieldOptions.forEach(function(fieldOption) {
                                    var option = {};
                                    option.id = idIndex;
                                    option.itemName = fieldOption;
                                    if (temp.indexOf(fieldOption) > -1) {
                                        option.selected = true;
                                    }
                                    else {
                                        option.selected = false;
                                    }
                                    idIndex++;
                                    options.push(option);
                                });
                                parameter.fieldOptions = options;
                            }
                            else {
                                parameter.fieldOptions = source.fieldOptions;
                            }

                            parameter.value = temp;
                            parameter.loaded = true;
                            parameter.loading = false;
                        });
                    });
                };

                var getParameter = function(name) {

                    var parameters = $scope.block.parameters;
                    for (var i = 0; i < parameters.length; i++){
                        if (parameters[i].name() == name)
                            return parameters[i];
                    }
                };

                $scope.setValue = function(parameter) {

                    parameter.dirty = true;
                    parameter.collected = true;

                    // check for dependants
                    if (parameter.name() in $scope.dependants){

                        var dependants = $scope.dependants[parameter.name()];

                        var dynamicSourceRequests = {
                            name: $scope.block.name,
                            parameters: [],
                            diagram: null
                        };
                        dependants.forEach(function(dependantName){

                            // retrieve the dependant parameter, clear value and mark as not loaded
                            var dependant = getParameter(dependantName);
                            dependant.value = "";
                            dependant.loaded = false;

                            var dependenciesCollected = true;
                            dependant.source().arguments.forEach(function(argument){

                                if(argument.type === 0){
                                    // represents a parameter dependency

                                    var dependencyName = argument.name;
                                    var dependency = getParameter(dependencyName);
                                    if (dependency.collected) {
                                        argument.value = dependency.value;
                                    }
                                    else{
                                        dependenciesCollected = false;
                                    }
                                }
                            });

                            if (dependenciesCollected) {
                                dependant.loading = true;
                                dynamicSourceRequests.parameters.push(dependant.data);
                            }
                        });

                        loadDynamicSources(dynamicSourceRequests);
                    }
                };

                // initialize
                init();
            }
        }
    }]);
