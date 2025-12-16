import React, { useState, useEffect, useLayoutEffect } from "react";
import Box from "@mui/material/Box";
import { IconButton } from "@mui/material";
import NavigateBeforeIcon from "@mui/icons-material/NavigateBefore";
import NavigateNextIcon from "@mui/icons-material/NavigateNext";
import Slide from "@mui/material/Slide";
import Stack from "@mui/material/Stack";
import {
  InteractableCardGroup,
  InteractableCard,
  Tooltip,
} from "@salt-ds/core";
import moment from "moment";

function Carousel(props) {
  const { batches, modelTypeSelected, batchIDselected } = props;
  const [cards, setCards] = useState([]);
  // currentPage is the current page of the cards that is currently displayed
  const [currentPage, setCurrentPage] = useState(0);
  // slideDirection is the direction that the cards will slide in
  const [slideDirection, setSlideDirection] = useState("left");
  const [batchID, setBatchID] = useState(batches[0].action);
  const [cardsPerPage, setCardsPerPage] = useState(4);

  // these two functions handle changing the pages

  useLayoutEffect(() => {
    function updateSize() {
      if (window.innerWidth > 1500) {
        setCardsPerPage(4);
      } else {
        setCardsPerPage(3);
      }
    }
    window.addEventListener("resize", updateSize);
    updateSize();
    return () => window.removeEventListener("resize", updateSize);
  }, []);

  //   useEffect(() => {
  //     useWindowSize();
  //     window.addEventListener("resize", useWindowSize);
  //     return () => window.removeEventListener("resize", useWindowSize);
  //   }, []);

  const handleNextPage = () => {
    setSlideDirection("left");
    setCurrentPage((prevPage) => prevPage + 1);
  };

  const handlePrevPage = () => {
    setSlideDirection("right");
    setCurrentPage((prevPage) => prevPage - 1);
  };

  const getBatchId = (item) => {
    setBatchID(item.action);
    batchIDselected(item.action);

    //  setIsSelected(e.target.index);
  };
  function dateFormater(params) {
      return moment(params).format("MMM D YY, h:mm a");
    //return moment(params).format("MM/DD/YY");
  }

  // const containerWidth = cardsPerPage * 350; // 250px per card

  const content = (des, catlog) => {
    return (
      <ul style={{ paddingLeft: 0, margin: 0 }} className="toolTipWrapper">
        <li>{des}{catlog}</li>
      </ul>
    );
    <></>;
  };

  return (
    //  outer box that holds the carousel and the buttons
    <Box
      sx={{
        display: "flex",
        flexDirection: "row",
        alignItems: "center",
        alignContent: "center",
        justifyContent: "center",
        height: "auto",
        width: "100%",
        marginTop: "40px",
      }}
    >
      {batches && batches.length > 0 && (
        <>
          <IconButton
            onClick={handlePrevPage}
            sx={{ margin: 0 }}
            disabled={currentPage === 0}
          >
            {/* this is the button that will go to the previous page you can change these icons to whatever you wish*/}
            <NavigateBeforeIcon />
          </IconButton>
          <Box sx={{ width: `100%`, height: "100%", padding: "0 10px" }}>
            {/* this is the box that holds the cards and the slide animation,
        in this implementation the card is already constructed but in later versions you will see how the
        items you wish to use will be dynamically created with the map method*/}
            {batches.map((card, index) => (
              <Box
                key={`card-${index}`}
                sx={{
                  width: "100%",
                  height: "100%",
                  display: currentPage === index ? "block" : "none",
                }}
                className={`carouselBlock`}
              >
                <Slide direction={slideDirection} in={currentPage === index}>
                  <Stack
                    spacing={2}
                    direction="row"
                    alignContent="center"
                    justifyContent="center"
                    sx={{ width: "100%", height: "100%" }}
                  >
                    <InteractableCardGroup
                      onChange={(e, item) => getBatchId(item)}
                      value={batchID}
                    >
                      {batches &&
                        batches
                          .slice(
                            index * cardsPerPage,
                            index * cardsPerPage + cardsPerPage
                          )
                          .map(function (item, i) {
                            const accent = item.action === batchID ? "top" : "";
                            const displayDate = dateFormater(item.createdDate);
                            const anomalyDisplay= `Anomaly Type : `;
                            const dateCreatedOn = "Batch created on: ";
                            const batchName = `Batch Name : ${item.name}`;
                            const predictedAnomalies = "Predicted Anomalies : ";
                            const actualAnomalies = "Actual Anomalies : ";
                            const modelVersion = `Model Version : ${item.modelVersion}`;
                            
                            return (
                              <>
                                <InteractableCard
                                  value={item}
                                  accent={accent}
                                  key={i}
                                >
                                  <div className="grid-container">
                                    <Tooltip content={content(batchName)}>
                                      <div className="grid-item leftField noPading">
                                        {item.name} 
                                      </div>
                                    </Tooltip>
                                    <Tooltip
                                      content={content(modelVersion)}
                                    >
                                      <div className="grid-item floatRight noPading ">
                                      {item.modelVersion}
                                      </div>
                                    </Tooltip>
                                    <Tooltip
                                      content={content("Anomaly Type")}
                                    >
                                      <div className="grid-item leftField ">
                                      Anomaly Type : 
                                      </div>
                                    </Tooltip>
                                   
                                    <Tooltip
                                      content={content(anomalyDisplay,item.anomalyDesc)}
                                    >
                                      <div className="grid-item floatRight mediumWidth">
                                         {item.anomalyDesc}
                                      </div>
                                    </Tooltip>
                                      {/* <div className="grid-item floatRight textAlginCenter stateSuccess">
                                        Completed
                                      </div> */}
                                    <Tooltip
                                      content={content("Anomaly Stats")}
                                    >
                                   <div className="grid-item leftField noPading">
                                   Anomaly Stats :
                                      </div> 
                                   </Tooltip>
                                    <Tooltip
                                      content={content(predictedAnomalies,item.anomalousRecordCount)}
                                    >
                                      <div className="grid-item floatRight fontBold mediumWidth2">
                                        {item.anomalousRecordCount}
                                      </div>
                                    </Tooltip>
                                    <Tooltip content={content(dateCreatedOn, displayDate)}>
                                      <div className="grid-item leftField1">
                                        {dateFormater(item.createdDate)}
                                      </div>
                                    </Tooltip>
                                    <Tooltip
                                      content={content(actualAnomalies,item.actualAnomalousRecordCount)}
                                    >
                                      <div className="grid-item floatRight mediumWidth1">
                                        {item.actualAnomalousRecordCount}
                                      </div>
                                    </Tooltip>
                                  </div>
                                </InteractableCard>
                              </>
                            );
                          })}
                    </InteractableCardGroup>
                  </Stack>
                </Slide>
              </Box>
            ))}
          </Box>
          <IconButton
            onClick={handleNextPage}
            sx={{
              margin: 0,
            }}
            disabled={
              currentPage >= Math.ceil((batches.length || 0) / cardsPerPage) - 1
            }
          >
            <NavigateNextIcon />
          </IconButton>
        </>
      )}
    </Box>
  );
}

export default Carousel;
