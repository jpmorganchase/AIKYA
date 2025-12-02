
import  React,{useEffect, useRef, useState} from "react";
import Dialog from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import { useNavigate } from "react-router-dom";
import ReactPlayer from 'react-player/lazy';
import "src/styles/theme.css";
import ScrollHighlightNabbar from "../components/ScrollHighlightNabbar/ScrollHighlightNabbar";


const Landing = () => {
    const navigate = useNavigate();
    const section1Ref = useRef();
    const section2Ref = useRef();
    const section3Ref = useRef();
    const section4Ref = useRef();
    const section5Ref = useRef();
    const section6Ref = useRef();
    const section7Ref = useRef();
    const [largeImage, setLargeImage] = useState(false);
    
  
    const navHeader = [
      {
        headerTitle: "Home",
        headerRef: section1Ref,
        headerID: "home"
      },
      {
        headerTitle: "Features",
        headerRef: section2Ref,
        headerID: "feaures"
      },
      {
        headerTitle: "Documentation",
        headerRef: section3Ref,
        headerID: "Documentation"
      },
      {
        headerTitle: "Demo",
        headerRef: section4Ref,
        headerID: "demo_full"
      },
      
        {
            headerTitle: "Architecture",
            headerRef: section5Ref,
            headerID: "Architecture"
          },
        {
          headerTitle: "Why Aikya?",
          headerRef: section6Ref,
          headerID: "why_aikya"
        },
        {
          headerTitle: "Login",
          headerRef: section7Ref,
          headerID: "login"
        }
    ];

   
    const handleModalClose = () => {
        setLargeImage(false);
      };

    const showDetailsModal = () => {
        setLargeImage(true);
      };
     
        function scrollHeader() {
         if (typeof window != "undefined" && document.getElementById("navigationBar") && document.getElementById("navigationBar") != null) { 
            var header = document.getElementById("navigationBar");
            var sticky = header.offsetTop;
          if (window.pageYOffset > sticky) {
            header.classList.add("sticky");
          } else {
            header.classList.remove("sticky");
          }
        }
      }

    useEffect(() => {
        userAuthVerify();
        window.onscroll = function() {scrollHeader()};
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const userAuthVerify = () => {
        const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
        if (userToken !== null && userToken !== undefined && userToken !== "") {
            navigate("/app/home");
        }
    };
    const renderLayout = () => {
        return (
         <>
             <div className="navbar navbar-expand-lg navbar-light bg-white">
        <div className="containerWrap" id="navigationBar">
            <a className="navbar-brand w-8" href="#" data-config-id="brand">
                <img src="/static/globe_icon.png" width="220" height="80" alt="" />
            </a>

            <button className="navbar-toggler shadow-none ms-2" type="button" data-bs-toggle="collapse" data-bs-target="#navigation" aria-controls="navigation" aria-expanded="false" aria-label="Toggle navigation">
                <span className="navbar-toggler-icon mt-2">
                    <span className="navbar-toggler-bar bar1"></span>
                    <span className="navbar-toggler-bar bar2"></span>
                    <span className="navbar-toggler-bar bar3"></span>
                </span>
            </button>
            <div className="navbar">
                {/* <a href="" >
                            Home
                        </a>
                <a href="#feaures" >
                    Features
                </a>
                <a href="#why_aikya" >
                            Why Aikya?
                </a>

                <a href="#demo_full" >
                            Demo
                        </a>
                <div className="subnav">
                    <button className="subnavbtn">How It Works?  <i className="fa fa-caret-down"></i></button>
                    <div className="subnav-content">
                                <a href="#Documentation">Documentation</a>
                                <a href="#architecture">Architecture</a>
                                <a href="#architecture">Why Blockchain</a>
                                <a href="#architecture">Integration with Aikya</a>
                    </div>
                </div> 
                
                <a href="#signin" onClick={(e)=>setActiveTab(e)}   >
                            Login
                        </a> */}
                    <ScrollHighlightNabbar section3Ref={section3Ref} navHeader={navHeader} />
            </div>
            
        </div>
    </div>
    <div>
        <div id="carouselExampleControls" className="containerWrap">
            <div className="carousel-inner" id="Home" ref={section1Ref}>
               
                <div className=" active">
                    <div className="page-header min-vh-40" >
                        <span className="mask bg-gradient-dark-landing"></span>
                        <div className="containerWrap">
                            <div className="row">
                                <div className="col-lg-8 mx-auto text-center my-auto">
                                 {/* <source src="/static/AikyaLanding.mp4" type="video/mp4"/> */}
                                 {/* <video autoPlay loop muted className="w-full h-full object-cover">
                                    <source src="/static/AikyaLanding.mp4" type="video/mp4" />
                                    testing
                                </video> */}
                                    <h2 className="text-white display-1-landing font-weight-bolder fadeIn2 fadeInBottom" >Empower Collaborative Insights</h2>
                                    <p className="lead text-white opacity-8 fadeIn3 fadeInBottom">Privacy preserving collaboration over permissioned network.</p>
                                    {/* <a href="" className="btn btn-primary btn-lg" onClick={(e)=>setActiveTab(e)} >DEMO</a> */}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
         
        </div>
    </div>
    <div className="" >
        <div className="containerWrap py-5">
            <div className="row align-items-center">
                <div className="col-md-6 mb-md-0 mb-4 pe-5">
                    <h3 className="mb-3" >Power of privacy protected collaboration</h3>
                    <p className="mb-md-5 mb-4">
                        Unlock the potential of collaborative fraud detectio, ristk assessment without compromising customer data privacy.  Aikya's federated learning technology keeps your data secure while fosterig groundbreaking discoveries across the financial sector.
                    </p>
                    <a className="btn bg-gradient-primary">Read more</a>
                </div>
                <div className="col-md-6">
                    <div className="blur-shadow-image text-center">
                    <div className='player-wrapper'>
                            <ReactPlayer
                            className='react-player'
                            url= '/static/demo_video.mp4'
                            width='100%'
                            height='100%'
                            controls = {true}

                            />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div className="py-5" id="feaures" ref={section2Ref}>
        <div className="containerWrap">
            <div className="row ">
                <div className="col-md-6">
                    <h3 className="mb-4" >Features</h3>
                    <p className="mb-4">Aikya leverages a combination of privacy enhancing technologies ( PETs) for enabling collaborative insights among member institutions without sharing the underlying data.</p>
                </div>
                <div className="col-md-6 text-md-end my-auto">
                    <a href="" className="btn btn-outline-primary">Read more</a>
                </div>
                <div className="col-md-6 mb-4">
                    <div className="info bg-white shadow-lg p-4 border-radius-lg">
                        <div className="icon mx-auto text-dark">
                            <i className="fa fa-envelope text-gradient fa-2x text-primary" aria-hidden="true"></i>
                        </div>
                        <h5 className="mt-3 text-dark">Cutting Edge Tech</h5>
                        <p className="text-sm">Built using the state-of-art Federated Learning basedd technology, Aikya leads the charge in technological advancement.</p>
                    </div>
                </div>
                <div className="col-md-6 mb-4">
                    <div className="info bg-white shadow-lg p-4 border-radius-lg">
                        <div className="icon mx-auto text-dark">
                            <i className="fa fa-database text-gradient fa-2x text-primary" aria-hidden="true"></i>
                        </div>
                        <h5 className="mt-3 text-dark">Blockchain</h5>

                        <p className="text-sm">Aikya empowers institutions to leverage blockchain's security for robust data protection. </p>
                    </div>
                </div>
                <div className="col-md-6 mb-4">
                    <div className="info bg-white shadow-lg p-4 border-radius-lg">
                        <div className="icon mx-auto text-dark">
                            <i className="fa fa-bluetooth text-gradient fa-2x text-primary" aria-hidden="true"></i>
                        </div>
                        <h5 className="mt-3 text-dark">Collaboration</h5>

                        <p className="text-sm">Harness the power of federated networks by collaboration without compromising data anonymity.</p>
                    </div>
                </div>
                <div className="col-md-6 mb-4">
                    <div className="info bg-white shadow-lg p-4 border-radius-lg">
                        <div className="icon mx-auto text-dark">
                            <i className="fa fa-retweet text-gradient fa-2x text-primary" aria-hidden="true"></i>
                        </div>
                        <h5 className="mt-3 text-dark">Privacy</h5>

                        <p className="text-sm">Safeguards sensitive institutional data with best in class privacy preservation and anonymity.</p>
                    </div>
                </div>
              
            </div>
        </div>
    </div>

    <div className="" id="Documentation" ref={section3Ref}>
        <div className="containerWrap py-5" >
            <div className="row align-items-center">
            <h2 >How it works</h2>
           
                    <div className='player-wrapper'>
                            <ReactPlayer
                            className='react-player'
                            url= '/static/aikya-HowItWorks.mp4'
                            playing
                            light='/static/HowItWorksIcon.png'
                            width='100%'
                            height='100%'
                            controls = {true}

                            />
                        </div>
                    
            </div>
        </div>
    </div>
    
    <div className="" id="demo_full" ref={section4Ref}>
        <div className="containerWrap py-5" id="demo_full">
            <div className="row align-items-center">
            <h2 >Demo</h2>
           
                    <div className='player-wrapper'>
                            <ReactPlayer
                            className='react-player'
                            url= '/static/demo_full.mov'
                            playing
                            light='/static/demo-icon.png'
                            width='100%'
                            height='100%'
                            controls = {true}

                            />
                        </div>
                    
            </div>
        </div>
    </div>
    <div className="" id="Architecture" ref={section5Ref}>
        <div className="containerWrap py-5" id="">
            <div className="row align-items-center">
            <h2 >Architecture</h2>
                    { largeImage &&      <Dialog
              open={true}
              maxWidth="lg"
              onClose={handleModalClose}
              aria-labelledby="form-dialog-title"
              className="biggerImageModal"
            >
              <DialogContent>
              <img  src="/static/architecture.png" width="100%" height="100%" alt='' />
              </DialogContent>
            </Dialog>
                    }
                    <div className='player-wrapper'>
                           <img className={`imagestyle`} src="/static/architecture.png" width="60%" height="70%" alt='' onClick={showDetailsModal} />
                        </div>
                    
            </div>
        </div>
    </div>
    <div className="py-0" id="why_aikya" ref={section6Ref}>
        <div className="row">
            <div className="col-6 text-center mx-auto mt-5 mb-4">
                <h2 >Why choose Aikya?</h2>
                <p>
                </p>
            </div>
        </div>
    </div>
    <div className="">
        <div className="containerWrap">
            <div className="row pt-5">
                <div className="col-md-4">
                    <div className="info">
                        <div className="icon icon-shape bg-gradient-dark p-3 shadow text-center d-flex align-items-center justify-content-center mb-4">
                            <span className="text-white text-sm font-weight-bolder">1</span>
                        </div>
                        <h6>Strengthen your compliance posture</h6>
                        <p>Integrates with your existing financial data infrastructure.  Gain regulatory compliance with complete confidence.  Empower leadership with privacy-compliant data insights.</p>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="info">
                        <div className="icon icon-shape bg-gradient-dark p-3 shadow text-center d-flex align-items-center justify-content-center mb-4">
                            <span className="text-white text-sm font-weight-bolder">2</span>
                        </div>
                        <h6>Stay ahead of data privacy regulations.</h6>
                        <p>Data privacy regulations are becoming stricter and the cost of non-compliance is significant.  Aikya protects data privacy with high degree of anonymity for generating insights.</p>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="info">
                        <div className="icon icon-shape bg-gradient-dark p-3 shadow text-center d-flex align-items-center justify-content-center mb-4">
                            <span className="text-white text-sm font-weight-bolder">3</span>
                        </div>
                        <h6>Seamless integration</h6>
                        <p>Aikya integrates with your existing data infrastructure.  Empowers privacy-preserving and compliant data insights.</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
   
    <div className="py-5 bg-gradient-dark position-relative">
        <div className="containerWrap position-relative z-index-2">
            <div className="row">
                <div className="col-md-7 mx-auto text-center">
                    <h3 className="text-white mb-3" >
                        Ready to get started?</h3>
                    <p className="text-white" >Start your trial with Aikya</p>
                    <a href="" className="btn btn-primary">Get in touch</a>
                </div>
            </div>
        </div>
    </div>
    <footer className="footer py-5">
        <div className="containerWrap">
            
            <hr className="horizontal dark mt-lg-5 mt-4 mb-sm-4 mb-1" />
            <div className="row">
                <div className="col-8 mx-lg-auto text-lg-center">
                    <p className="text-sm text-secondary">
                        Copyright © 2025 AIKYA
                    </p>
                </div>
            </div>
        </div>
    </footer>
         </>  
        );
    };

    return renderLayout();
};

export default Landing;