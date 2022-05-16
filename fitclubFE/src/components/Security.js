import React, { Component } from 'react'
import { connect } from 'react-redux';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
// import { Link } from 'react-router-dom';
import exclamationSecurity from '../assets/exclamationSecurity.png';
import * as apiCalls from '../api/apiCalls';
import changePassword from '../assets/changePassword.png';
import changeEmail from '../assets/changeEmail.png';
import ButtonWithProgressForEmails from './ButtonWithProgressForEmails';

class Security extends Component {
    state = {
        id: this.props.loggedInUser.id,

        //
        pendingApiCallChangeEmail: false,
        successfullyMessageChangeEmail: false,
        setButtonDisabledChangeEmail: false,
        apiErrorChangeEmail: false,
        isChangeMailSentSuccessfully: false,

        //
        pendingApiCallChangePassword: false,
        successfullyMessageChangePassword: false,
        setButtonDisabledChangePassword: false,
        apiErrorChangePassword: false,
        isChangePasswordSentSuccessfully: false,
    };

    changeEmail = () => {
        this.setState({ pendingApiCallChangeEmail: true });
        apiCalls.changeEmail(this.state.id, this.props.loggedInUser.jwt)
            .then((response) => {
                this.setState({
                    pendingApiCallChangeEmail: false,
                    successfullyMessageChangeEmail: true,
                    setButtonDisabledChangeEmail: true,
                    isChangeMailSentSuccessfully: true,
                })
            })
            .catch((error) => {
                if (error.response) {
                    this.setState({
                        apiErrorChangeEmail: error.response.data.message,
                        pendingApiCallChangeEmail: false,
                        isChangeMailSentSuccessfully: false,
                    });
                }
            });
    }

    changePassword = () => {
        this.setState({ pendingApiCallChangePassword: true });
        apiCalls.changePassword(this.state.id, this.props.loggedInUser.jwt)
            .then((response) => {
                this.setState({
                    pendingApiCallChangePassword: false,
                    successfullyMessageChangePassword: true,
                    setButtonDisabledChangePassword: true,
                    isChangePasswordSentSuccessfully: true
                })
            })
            .catch((error) => {
                if (error.response) {
                    this.setState({
                        apiError: error.response.data.message,
                        pendingApiCallChangePassword: false,
                        isChangePasswordSentSuccessfully: false
                    });
                }
            });
    }

    render() {

        return (
            <React.Fragment>

                <div className="card mt-4 verticalLineSecurity">
                    <Row>
                        <Col xs={11} md={11} lg={11} xl={11}>
                            <div className="card-body d-flex flex-column mt-1">
                                <p className="text-secondary textSecurityTop mb-0">
                                    DO NOT share your personal information via FitClub platform.
                                </p>
                            </div>
                        </Col>
                        <Col xs={1} md={1} lg={1} xl={1}>
                            <div className="d-flex justify-content-center securityMessageSubmit mt-1">
                                <img className="m-auto" src={exclamationSecurity} width="32" alt="SecurityAlert" />
                            </div>
                        </Col>
                    </Row>

                    <Row>
                        <Col xs={11} md={11} lg={11} xl={11}>
                            <div className="card-body d-flex flex-column mt-1">
                                <p className="text-secondary textSecurityTop mb-0">
                                    FitClub will NEVER ask you for sensitive information via e-mail.
                                </p>
                            </div>
                        </Col>
                        <Col xs={1} md={1} lg={1} xl={1}>
                            <div className="d-flex justify-content-center securityMessageSubmit mt-1">
                                <img className="m-auto" src={exclamationSecurity} width="32" alt="SecurityAlert" />
                            </div>
                        </Col>
                    </Row>

                    <Row>
                        <Col xs={11} md={11} lg={11} xl={11}>
                            <div className="card-body d-flex flex-column mt-1 mb-1">
                                <p className="text-secondary textSecurityTop mb-0">
                                    Avoid clicking on suspicious links received in e-mails from unknown sources.&nbsp;
                                </p>
                            </div>
                        </Col>
                        <Col xs={1} md={1} lg={1} xl={1}>
                            <div className="d-flex justify-content-center securityMessageSubmit mt-1">
                                <img className="m-auto" src={exclamationSecurity} width="32" alt="SecurityAlert" />
                            </div>
                        </Col>

                    </Row>
                </div>


                <div className="card mt-4 p-2 shadow-sm">
                    <Row>
                        <Col xs={12} md={12} lg={12} xl={8}>
                            <div className="card-body d-flex flex-column">

                                <div //  text-center 
                                    className="card-title text-security-changes pb-2">
                                    Change Email
                                </div>

                                <h6 className="text-login-page text-secondary notClickable-text">
                                    <span className="">Change your email address by clicking the activation
                                        link in the email that will be sent to your current address.<br></br>
                                        You will need to login again after this change.
                                    </span>
                                </h6>

                                {this.state.isChangeMailSentSuccessfully && (
                                    <h5 className="text-success font-weight-bold pt-4 text-center text-resend">
                                        <span className="far fa-check-circle fa-lg mb-1"></span>
                                        <span className="">&nbsp;Email was successfully sent!</span>
                                    </h5>
                                )}

                                <div className="text-center mt-4">
                                    <ButtonWithProgressForEmails
                                        onClick={this.changeEmail}
                                        // disabled={disableSubmit || this.state.pendingApiCall}
                                        disabled={this.state.setButtonDisabledChangeEmail}
                                        pendingApiCall={this.state.pendingApiCallChangeEmail}
                                        value="Change Email&nbsp;&nbsp;"
                                    />
                                </div>
                            </div>
                        </Col>

                        <Col xs={12} md={12} lg={12} xl={4}>
                            <div className="d-flex justify-content-center pr-5 pt-5 pb-2 ">
                                <img className="m-auto" src={changeEmail} width="112" alt="ChangeEmail" />
                            </div>
                        </Col>

                    </Row>
                </div>


                {/* Change Password */}

                <div className="card mt-4 p-2 shadow-sm">
                    <Row>
                        <Col xs={12} md={12} lg={12} xl={8}>
                            <div className="card-body d-flex flex-column">

                                <div //  text-center 
                                    className="card-title text-security-changes pb-2">
                                    Change Password
                                </div>

                                <h6 className="text-login-page text-secondary notClickable-text">
                                    <span className="">You will be sent an email with instructions on how to change your password.<br></br>
                                        Are you sure you want to do this?
                                    </span>
                                </h6>

                                {this.state.isChangePasswordSentSuccessfully && (

                                    <h5 className="text-success font-weight-bold pt-3 text-center success-text-resend">
                                        <span className="far fa-check-circle fa-lg fa-2x"></span>
                                        <span className="">&nbsp;Email was successfully sent!</span>
                                    </h5>
                                )}

                                <div className="text-center mt-3">
                                    <ButtonWithProgressForEmails
                                        onClick={this.changePassword}
                                        // disabled={disableSubmit || this.state.pendingApiCall}
                                        disabled={this.state.setButtonDisabledChangePassword}
                                        pendingApiCall={this.state.pendingApiCallChangePassword}
                                        value="Send Email&nbsp;&nbsp;"
                                    />
                                </div>
                            </div>
                        </Col>

                        <Col xs={12} md={12} lg={12} xl={4}>
                            <div className="d-flex justify-content-center pr-5 pt-5 pb-2">
                                <img className="m-auto" src={changePassword} width="230" alt="Hoaxify" />
                            </div>
                        </Col>

                    </Row>
                </div>
            </React.Fragment>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        loggedInUser: state
    }
}

export default connect(mapStateToProps)(Security);