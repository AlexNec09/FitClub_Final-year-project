import React, { useState, useEffect } from "react";
import Input from "../components/Input";
import ButtonWithProgress from "../components/ButtonWithProgress";
import { connect } from "react-redux";
import * as authActions from "../redux/authActions";
import loginImg from '../assets/login-image.png';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

export const LoginPage = (props) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [apiError, setApiError] = useState();
  const [pendingApiCall, setPendingApiCall] = useState(false);

  useEffect(() => {
    setApiError();
  }, [username, password]);

  const onClickLogin = () => {
    const body = {
      username,
      password,
    };
    setPendingApiCall(true);
    props.actions
      .postLogin(body)
      .then((response) => {
        setPendingApiCall(false);
        props.history.push("/");
        // window.location.reload();
      })
      .catch((error) => {
        if (error.response) {
          setPendingApiCall(false);
          setApiError(error.response.data.message);
        }
      });
  };

  let disableSubmit = false;
  if (username === "" || password === "") {
    disableSubmit = true;
  }

  return (

    <div className="background-login-image pt-5" id="background-login-image">
      <div className="p-4">
        <Row>
          <Col className="ps-5" xs={10} md={10} lg={10} xl={7}>
            <div className="d-flex justify-content-center ps-4">
              <img className="" src={loginImg} width="610" alt="LoginImg" />
            </div>
          </Col>

          <Col className="" xs={10} md={10} lg={10} xl={3}>
            <h2 className="headerTitle">Welcome!</h2>
            <label className="info-label">Please login with your personal information by username and password&nbsp;&nbsp;

              <span className="fa fa-lock "></span>

            </label>
            <div className="card-body pt-2">
              <div className="form-group py-4">
                <Input
                  name="username"
                  label="Username"
                  placeholder="Your username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                />
              </div>

              <div className="form-group py-2">
                <Input
                  name="password"
                  label="Password"
                  placeholder="Your password"
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                />
              </div>

              <a className="forgot-password" href="#/forgotPassword">
                <p className="pt-1 text-end">Forgot Password?</p>
              </a>

              <div className="text-center pt-2 pb-1">
                <ButtonWithProgress className="custom-row-button"
                  onClick={onClickLogin}
                  disabled={disableSubmit || pendingApiCall}
                  pendingApiCall={pendingApiCall}
                  text="Login"
                />
              </div>

              {apiError && (
                <div className="pt-4 pb-3 text-center">
                  <div className="d-inline alert alert-danger row shadow p-2 rounded">
                    <i className="d-inline fas fa-exclamation-triangle icon-exclamation-login-error ml-auto" />
                    {apiError}&nbsp;&nbsp;</div>
                </div>
              )}

              <hr width="100%"></hr>
              <a className="not-on-fitclub" href="#/signup">
                <p className="text-center">Not on FitClub yet? Sign up</p>
              </a>
            </div>
          </Col>



        </Row>
      </div>
    </div>
  );
};

LoginPage.defaultProps = {
  actions: {
    postLogin: () =>
      new Promise((resolve, reject) => {
        resolve({});
      }),
  },
  dispatch: () => { },
};

const mapDispatchToProps = (dispatch) => {
  return {
    actions: {
      postLogin: (body) => dispatch(authActions.loginHandler(body)),
    },
  };
};

export default connect(null, mapDispatchToProps)(LoginPage);
