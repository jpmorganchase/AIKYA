import {
  DatePicker,
  DatePickerOverlay,
  DatePickerRangeInput,
  DatePickerRangePanel,
} from "@salt-ds/lab";
import { useCallback, useState, useRef } from "react";
import CloseIcon from '@mui/icons-material/Close';
import moment from "moment";

// Helper functions to replace removed exports from @salt-ds/lab
const getCurrentLocale = () => {
  return navigator.language || 'en-US';
};

const formatDate = (date, locale, options) => {
  if (!date) return null;
  return moment(date).format("YYYY-MM-DD");
};

export const DateRangePicker = (props) => {
  const { selectedFromDate, selectedTooDate } = props;
  const [selectedDate, setSelectedDate] = useState(null);
  const [disableClear, setDisableClear] = useState(false);

  const dateInput = useRef();

  function formatDateRange(
    dateRange,
    locale = getCurrentLocale(),
    options) {
    const { startDate, endDate } = dateRange || {};
    const formattedStartDate = startDate
      ? formatDate(startDate, locale, options)
      : startDate;
    const formattedEndDate = endDate
      ? formatDate(endDate, locale, options)
      : endDate;

    const fmtFromDate = dateFormater(formattedStartDate);
    const fmtTooDate = dateFormater(formattedEndDate);


    props.selectedFromDate(fmtFromDate);
    props.selectedTooDate(fmtTooDate);


    return `Start date: ${fmtFromDate}, End date: ${fmtTooDate}`;
  }
  function dateFormater(params) {
    //  return moment(params).format("MMM D YY, h:mm a");
    return moment(params).format("YYYYMMDD");
  }

  const handleSelectedDateChange = useCallback(
    (
      newSelectedDate

    ) => {
      console.log(`Selected date range: ${formatDateRange(newSelectedDate)}`);
      setSelectedDate(newSelectedDate);
      setDisableClear(true);
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [setSelectedDate],
  );

  const clearInput = (e) => {

    dateInput.current.children[0].children[0].value = null;
    dateInput.current.children[0].children[2].value = null;
    // setDisableClear(false);
    props.selectedFromDate(null);
    props.selectedTooDate(null);
    return false;

  }


  return (
    <>

      <div style={{ width: '350px', padding: '10px 5px' }}>
        <p> Batch Date Range</p>
        <DatePicker
          selectionVariant="range"
          selectedDate={selectedDate}
          onSelectedDateChange={handleSelectedDateChange}
          style={{ display: 'flex' }}
          ref={dateInput}
        >
          <DatePickerRangeInput bordered />
          {disableClear && <CloseIcon color="inherit" onClick={(e) => clearInput(e)} title="close" className='calendarCloseBtn'>
          </CloseIcon>
          }

          <DatePickerOverlay>
            <DatePickerRangePanel />
          </DatePickerOverlay>
        </DatePicker>
      </div>

    </>
  );
};

export default DateRangePicker;