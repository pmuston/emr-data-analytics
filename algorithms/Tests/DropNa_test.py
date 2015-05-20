__author__ = 'noelbell'

from Columns import Columns
from DropNa import DropNa
from DataBrick import DataBrick


def driver():
    blocks_to_execute = []

    DataBrick1 = DataBrick('DataBrick1','DataBrick_1')
    DataBrick1.parameters = {'Project': 'PICKLE-20150401', 'Query': {
  "query_name": "query1",
  "docType": "json",
  "version": "1.0",
  "timeSelector": [
    {
      "startTime": "2015-02-09T16:30:00.000Z",
      "endTime": "2015-02-11T02:16:30.000Z"
    },
    {
      "startTime": "2015-02-24T015:30:00.000Z",
      "endTime": "2015-02-26T10:30:00.000Z"
    },
    {
      "startTime": "2015-03-06T08:30:00.000Z",
      "endTime": "2015-03-10T16:30:00.000Z"
    }
  ],
  "sampleRateSecs": 1,
  "columns": [
    {
      "tag": "FT630B/DENS.CV",
      "alias": "Flow",
      "dataType": "Float",
      "renderType": "VALUE",
      "format": "0.###"
    },
    {
      "tag": "PT615/WIRED_PSIA.CV",
      "alias": "Pressure",
      "dataType": "Float",
      "renderType": "VALUE",
      "format": "0.###"
    },
    {
      "tag": "TT6079/INPUT_1.CV",
      "alias": "Temperature",
      "dataType": "Float",
      "renderType": "VALUE",
      "format": "0.###"
    },
    {
      "tag": "630_mass_fraction_c5",
      "alias": "630_MASS_FRAC_C5",
      "dataType": "Float",
      "renderType": "VALUE",
      "format": "0.###"
    }
  ]
}}
    DataBrick1.input_connectors = {}
    blocks_to_execute.append(DataBrick1)



    columns1 = Columns('Columns1', 'columns_1')
    columns1.parameters = {'Columns': ['Flow','Pressure','Temperature']}
    columns1.input_connectors = {'in': ['DataBrick_1/out']}
    blocks_to_execute.append(columns1)



    dropna = DropNa('ExpFilt1', 'dropna_1')
    dropna.input_connectors = {'in' : ['columns_1/out']}
    blocks_to_execute.append(dropna)



    results_table = {}

    for block in blocks_to_execute:
        results_table.update(block.execute(results_table))


def main():
    driver()

if __name__ == '__main__':
    main()