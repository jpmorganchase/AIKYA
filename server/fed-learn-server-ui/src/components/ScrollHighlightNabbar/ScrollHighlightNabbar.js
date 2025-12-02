import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import PropTypes from "prop-types";
import "./ScrollHighlightNabbar.css";

/**
 * @param {number} currentPosition Current Scroll position
 * @param {Array} sectionPositionArray Array of positions of all sections
 * @param {number} startIndex Start index of array
 * @param {number} endIndex End index of array
 * @return {number} Current Active index
 */
const nearestIndex = (
  currentPosition,
  sectionPositionArray,
  startIndex,
  endIndex
) => {
  if (startIndex === endIndex) return startIndex;
  else if (startIndex === endIndex - 1) {
    if (
      Math.abs(
        sectionPositionArray[startIndex].headerRef.current.offsetTop -
          currentPosition
      ) <
      Math.abs(
        sectionPositionArray[endIndex].headerRef.current.offsetTop -
          currentPosition
      )
    )
      return startIndex;
    else return endIndex;
  } else {
    var nextNearest = ~~((startIndex + endIndex) / 2);
    if(nextNearest &&  sectionPositionArray[nextNearest + 1 ].headerRef.current !== undefined && sectionPositionArray[nextNearest + 1 ].headerRef.current.offsetTop !== undefined ){
      var a = Math.abs(
        sectionPositionArray[nextNearest].headerRef.current.offsetTop -
          currentPosition
      );
      var b = Math.abs(
        sectionPositionArray[nextNearest + 1].headerRef.current.offsetTop -
          currentPosition
      );
      if (a < b) {
        return nearestIndex(
          currentPosition,
          sectionPositionArray,
          startIndex,
          nextNearest
        );
      } else {
        return nearestIndex(
          currentPosition,
          sectionPositionArray,
          nextNearest,
          endIndex
        );
      }
    }
    
  }
};

export default function ScrollHighlightNabbar({ navHeader }) {
  const [activeIndex, setActiveIndex] = useState(0);
  const navigate = useNavigate();
  useEffect(() => {
    const handleScroll = (e) => {
      var index = nearestIndex(
        window.scrollY,
        navHeader,
        0,
        navHeader.length - 1
      );
      setActiveIndex(index);
    };
    document.addEventListener("scroll", handleScroll);
    return () => {
      document.removeEventListener("scroll", handleScroll);
    };
  }, [navHeader]);

  const setActiveTab = (e) => {
    e.preventDefault();
    navigate("/signin");
}

  return (
    <ul className="navbar-nav navbar-nav-hover ms-auto">
                    <li className="nav-item mx-2">
                        <a href="#Home" className="nav-link ps-2 cursor-pointer" style={{ borderBottom: activeIndex === 0 ? "3px solid #F38A8A" : "" }}>
                            Home
                        </a>
                    </li>
                    <li className="nav-item mx-2">
                        <a href="#feaures" className="nav-link ps-2 cursor-pointer" style={{ borderBottom: activeIndex === 1 ? "3px solid #F38A8A" : "" }}>
                            Features
                        </a>
                    </li>
                    <li>
                    <div className="subnav">
                    <button className="subnavbtn" style={{ borderBottom: activeIndex === 2 ? "3px solid #F38A8A" : "" }}>How It Works?  <i className="fa fa-caret-down"></i></button>
                    <div className="subnav-content">
                                <a href="#Documentation">Documentation</a>
                                <a href="#architecture">Architecture</a>
                                <a href="#architecture">Why Blockchain</a>
                                <a href="#architecture">Integration with Aikya</a>
                    </div>
                </div> 
                    </li>
                   
                    <li className="nav-item mx-2">
                        <a href="#demo_full" className="nav-link ps-2 cursor-pointer" style={{ borderBottom: activeIndex === 3 ? "3px solid #F38A8A" : "" }}>
                            Demo
                        </a>
                    </li>
                 
                    <li className="nav-item mx-2">
                        <a href="#why_aikya" className="nav-link ps-2 cursor-pointer" style={{ borderBottom: activeIndex === 4 ? "3px solid #F38A8A" : "" }}>
                            Why Aikya?
                        </a>
                    </li>
                    <li className="nav-item mx-2">
                        <a href="#signin" onClick={(e)=>setActiveTab(e)}  className="nav-link ps-2 cursor-pointer" >
                            Login
                        </a>
                      
                    </li>
                    <li className="nav-item mx-2">
                        <a href="" className="nav-link ps-2 cursor-pointer">
                            <i className="fa fa-shopping-bag" aria-hidden="true"></i>
                        </a>
                    </li>
                </ul> 
    
     
    
  );
}

ScrollHighlightNabbar.propTypes = {
  navHeader: PropTypes.arrayOf(
    PropTypes.shape({
      headerID: PropTypes.string,
      headerRef: PropTypes.object.isRequired,
      headerTitle: PropTypes.string.isRequired
    })
  ).isRequired
};
