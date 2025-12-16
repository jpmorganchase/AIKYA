import * as React from "react";
import Plotly from "plotly.js-dist";

export default function PlotlyChart(shaplyData) {
  let rows = shaplyData.data;
  let rowLabel = [];

  rows.map(function (row) {
    let keyValue = row["name"];
    if (rowLabel.indexOf(keyValue) < 0) {
      rowLabel.push(row["name"]);
    }
  });

  let data = [];
  let rowData;
  rowLabel.map(function (items) {
    rows.map(function (row) {
      if (row["name"] === items) {
        rowData = row.values;
        let onePlot = {
          type: "violin",
          x: rowData,
          points: "none",
          box: {
            visible: false,
          },
          boxpoints: false,
          line: {
            color: "#2E96FF",
          },
          fillcolor: "#2E96FF",
          opacity: 0.4,
          meanline: {
            visible: true,
          },
          y0: items,
        };
        data.push(onePlot);
      }
    });
  });

  var layout = {
    title: {
      text: "",
    },
    showlegend: false,
    xaxis: {
      zeroline: false,
    },
    yaxis: {
      zeroline: false,
    },
    margin: { t: 0, r: 0, b: 50, l: 300 },
    // violinmode: "group"
  };

  Plotly.newPlot("violinChart", data, layout, { yaxis: { automargin: true } });

  return <>
  </>;
}
