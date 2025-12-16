import * as d3 from "d3";

// type VerticalViolinShapeProps = {
//   data: number[];
//   binNumber: number;
//   yScale: d3.ScaleLinear<number, number, never>;
//   width: number;
// };

export const VerticalViolinShape = ({
  data,
  xScale,
  yScale,
  width,
  binNumber,
}) => {
  const min = Math.min(...data);
  const max = Math.max(...data);

  const binBuilder = d3
    .bin()
    .domain([min, max])
    .thresholds(xScale.ticks(binNumber))
    .value((d) => d);
  const bins = binBuilder(data);

  const biggestBin = Math.max(...bins.map((b) => b.length));

  const wScale = d3
    .scaleLinear()
    .domain([-biggestBin, biggestBin])
    .range([0, yScale.bandwidth()]);

  const areaBuilder = d3
    .area()
    .x((d) => xScale(d.x0))
    .y0((d) => wScale(-d.length))
    .y1((d) => wScale(d.length))
    .curve(d3.curveBumpY);

  const areaPath = areaBuilder(bins);

  return (
    <path
      d={areaPath || undefined}
      opacity={1}
      stroke="black"
      fill="#2E96FF"
      fillOpacity={0.6}
      strokeWidth={1}
    />
  );
};
