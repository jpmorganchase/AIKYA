import { useEffect, useMemo, useRef } from "react";
import * as d3 from "d3";
import { VerticalViolinShape } from "./VerticalViolinShape";

const MARGIN = { top: 30, right: 30, bottom: 30, left: 250 };

// type ViolinProps = {
//   width: number;
//   height: number;
//   data: { name: string; value: number }[];
// };

export const Violin = ({ width, height, data }) => {
  // Layout. The div size is set by the given props.
  // The bounds (=area inside the axis) is calculated by substracting the margins
  const axesRef = useRef(null);
  const boundsWidth = width - MARGIN.right - MARGIN.left;
  const boundsHeight = height - MARGIN.top - MARGIN.bottom;

  // Compute everything derived from the dataset:
  const { min, max, groups } = useMemo(() => {
    const [min, max] = d3.extent(data.map((d) => d.value));
    const groups = data
      .map((d) => d.name)
      .filter((x, i, a) => a.indexOf(x) == i);
    return { min, max, groups };
  }, [data]);

 

    

  // // Compute scales
  // const yScale = d3
  //   .scaleLinear()
  //   .domain([min, max])
  //   .range([boundsHeight, 0])
  //   .nice();
  
  
  

  // const xScale = d3
  //   .scaleBand()
  //   .range([0, boundsWidth])
  //   .domain(groups)
  //   .padding(0.25);


    const yScale = d3
    .scaleBand()
    .domain(groups)
    .range([boundsHeight, 0])
    .padding(0.25);
   
  
  
  

  const xScale = d3
    .scaleLinear()
    .range([0, boundsWidth])
    .domain([min, max])
    .nice();

    
    const mouseover = function (d, groupData, group) {
      var tooltip = d3.select("body").append("div")   
    .attr("class", "tooltip")               
    .style("opacity", 0);
      console.log("tooltip");
      tooltip.transition().duration(200).style("opacity", 0.9);
      tooltip.html(`${group} `).style("left", ((d.pageX) + 10) + "px").style("top", ((d.pageY) + 10) + "px");

    };

    const mouseout = function (d) {
    //  tooltip.transition() .duration(500) .style("opacity", 0);
    document.querySelectorAll(".tooltip").forEach(el => el.remove());
    };

  // Render the X and Y axis using d3.js, not react
  useEffect(() => {
    const svgElement = d3.select(axesRef.current);
    svgElement.selectAll("*").remove();
    const xAxisGenerator = d3.axisBottom(xScale);
    svgElement
      .append("g")
      .attr("transform", "translate(0," + boundsHeight + ")")
      .call(xAxisGenerator);
     

    const yAxisGenerator = d3.axisLeft(yScale);
    svgElement.append("g").call(yAxisGenerator);
   
  }, [xScale, yScale, boundsHeight]);

  // Build the shapes
  const allShapes = groups.map((group, i) => {
    const groupData = data.filter((d) => d.name === group).map((d) => d.value);
    return (
      <g key={i} transform={`translate(0, ${yScale(group)})`} onMouseOver={(d)=> mouseover(d, groupData, group)} onMouseOut ={mouseout}>
        <VerticalViolinShape
          data={groupData}
          xScale={xScale}
          yScale={yScale}
          width={yScale.bandwidth()}
          binNumber={20}
        />
      </g>
    );
  });

  return (
    <div>
      <svg width={width} height={height} style={{ display: "inline-block" }}>
        {/* first group is for the violin and box shapes */}
        <g
          width={boundsWidth}
          height={boundsHeight}
          transform={`translate(${[MARGIN.left, MARGIN.top].join(",")})`}
        >
          {allShapes}
        </g>
        {/* Second is for the axes */}
        <g
          width={boundsWidth}
          height={boundsHeight}
          ref={axesRef}
          transform={`translate(${[MARGIN.left, MARGIN.top].join(",")})`}
        />
      </svg>
    </div>
  );
};
