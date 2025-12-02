import { isEmpty } from "lodash";
import React, { useEffect, useState } from "react";
import { Helmet } from "react-helmet";
import { useNavigate } from "react-router-dom";
import { importSignIn } from "src/events/importSignIn";
import { importSignUp } from "src/events/importSignUp";

const Login = (props) => {
    const navigate = useNavigate();

    let [authMode, setAuthMode] = useState("signin");

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [pass, setPass] = useState("");
    const [company] = useState(process.env.REACT_APP_BANK.toUpperCase());
    const [error, setError] = useState("");

    const changeAuthMode = () => {
        clearForm();
        setAuthMode(authMode === "signin" ? "signup" : "signin");
    };

    useEffect(() => {
        userAuthVerify();
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    const userAuthVerify = () => {
        const userToken = localStorage.getItem(process.env.REACT_APP_BANK + "loggedInfo");
        if (userToken !== null && userToken !== undefined && userToken !== "") {
            navigate("/app/home");
        }
    };

    async function fetchSignin() {
        let payload = {
            username: email,
            password: pass
        };

        let result = await importSignIn(payload);
        if (result && result.data) {
            if (result.data.statusCode === 200) {
                localStorage.setItem(process.env.REACT_APP_BANK + "loggedInfo", JSON.stringify(result.data));
                navigate("/app/home");
            } else {
                setError(result.data.message);
            }
        } else {
            setError("You have entered an invalid username or password.");
        }
    }

    async function fetchSignup() {
        let payload = {
            email: email,
            password: pass,
            name: name,
            company: company
        };
        let result = await importSignUp(payload);
        if (result && result.data) {
            if (result.data.statusCode === 200) {
                clearForm();
                changeAuthMode();
            } else {
                setError(result.data.message);
            }
        } else {
            setError("Something went wrong - please check again the form.");
        }
    }

    const clearForm = () => {
        setName("");
        // setCompany("");
        setPass("");
        setEmail("");
        setError("");
    };

    const onSignInSubmit = () => {
        if (validateForm()) {
            return;
        }
        fetchSignin();
    };

    const onSignUpSubmit = () => {
        if (validateForm()) {
            return;
        }
        fetchSignup();
    };

    const validateForm = () => {
        if (authMode === "signin") {
            if (isEmpty(pass) || isEmpty(email)) {
                setError("Please provide credentials.");
                return true;
            }
        } else {
            if (isEmpty(name) || isEmpty(pass) || isEmpty(email)) {
                setError("Please provide all the details.");
                return true;
            }
        }
        return false;
    };

    const signupLayout = () => {
        return (
            <div className="Auth-form-container">
                <Helmet>
                    <title>{process.env.REACT_APP_BANK ? process.env.REACT_APP_BANK.toUpperCase() : ""}</title>
                </Helmet>
                <form className="Auth-form">
                    <div className="Auth-form-content">
                        <h3 className="Auth-form-title">Sign Up</h3>
                        <div className="text-center">
                            Already registered?{" "}
                            <span className="link-primary" onClick={changeAuthMode}>
                                Sign In
                            </span>
                        </div>
                        <div className="form-group mt-3">
                            <label>Name</label>
                            <input
                                type="text"
                                value={name}
                                name={name}
                                autoComplete={"off"}
                                onChange={(e) => setName(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Name"
                            />
                        </div>
                        <div className="form-group mt-3">
                            <label>Email Address</label>
                            <input
                                type="email"
                                value={email}
                                name={email}
                                autoComplete={"off"}
                                onChange={(e) => setEmail(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Email Address"
                            />
                        </div>
                        <div className="form-group mt-3">
                            <label>Password</label>
                            <input
                                type="password"
                                value={pass}
                                name={pass}
                                autoComplete={"off"}
                                onChange={(e) => setPass(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Password"
                            />
                        </div>
                        {/* <div className="form-group mt-3">
                            <label>Company</label>
                            <input
                                type="text"
                                value={company}
                                name={company}
                                autoComplete={"off"}
                                onChange={(e) => setCompany(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Company"
                            />
                        </div> */}
                        {error && (
                            <>
                                <div className="form-group mt-3">
                                    <p className="error">{error}</p>
                                </div>
                            </>
                        )}
                        <div className="d-grid gap-2 mt-3">
                            <button type="button" className="btn btn-primary" onClick={onSignUpSubmit}>
                                Submit
                            </button>
                        </div>
                        {/* <p className="text-center mt-2">
                            Forgot <a href="#">password?</a>
                        </p> */}
                    </div>
                </form>
            </div>
        );
    };

    const signinLayout = () => {
        return (
            <div className="Auth-form-container">
                <Helmet>
                    <title>{process.env.REACT_APP_BANK ? process.env.REACT_APP_BANK.toUpperCase() : ""}</title>
                </Helmet>
                <form className="Auth-form">
                    <div className="Auth-form-content">
                        <h3 className="Auth-form-title">Sign In</h3>
                        <div className="text-center">
                            Not registered yet?{" "}
                            <span className="link-primary" onClick={changeAuthMode}>
                                Sign Up
                            </span>
                        </div>
                        <div className="form-group mt-3">
                            <label>Email address</label>
                            <input
                                type="email"
                                value={email}
                                name={email}
                                autoComplete={"off"}
                                onChange={(e) => setEmail(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Enter email"
                            />
                        </div>
                        <div className="form-group mt-3">
                            <label>Password</label>
                            <input
                                type="password"
                                value={pass}
                                name={pass}
                                autoComplete={"off"}
                                onChange={(e) => setPass(e.target.value)}
                                className="form-control mt-1"
                                placeholder="Enter password"
                            />
                        </div>
                        {error && (
                            <>
                                <div className="form-group mt-3">
                                    <p className="error">{error}</p>
                                </div>
                            </>
                        )}
                        <div className="form-group mt-3">
                            <button type="button" className="btn btn-primary" onClick={onSignInSubmit}>
                                Submit
                            </button>
                        </div>{" "}
                    </div>
                </form>
            </div>
        );
    };

    const renderLayout = () => {
        if (authMode === "signin") {
            return signinLayout();
        }

        return signupLayout();
    };

    return renderLayout();
};

export default Login;
