import * as React from 'react';
import { BarChart } from '@mui/x-charts/BarChart';

const uData = [4000, 3000, 2000, 2780, 1890, 2390, 3490];
const pData = [2400, 1398, 9800, 3908, 4800, 3800, 4300];
const xLabels = [
  'June',
  'July',
  'Aug',
  'Sept',
  'Oct',
  'Nov',
  'Dec',
];

export default function SimpleBarChart() {
  return (
    <>
    <div style={{padding:"5px", fontWeight:"bold"}}> Model Prediction *</div>
    <BarChart
      height={300}
      style={{margin:'0 auto'}}
      series={[
        { data: pData, label: 'Actual Anomalies', id: 'pvId' },
        { data: uData, label: 'Predicted Anomalies', id: 'uvId' },
      ]}
      xAxis={[{ data: xLabels, scaleType: 'band' }]}
      slotProps={{
        legend: {
          direction: 'row',
          position: { vertical: 'top', horizontal: 'right' }, 
          padding: 0,
        },
      }}
    />
    </>
  );
}