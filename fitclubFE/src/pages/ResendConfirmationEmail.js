import React, { Component } from 'react'
import { connect } from 'react-redux';
import image from '../assets/confirmationEmail.png';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';
import ButtonWithProgress from '../components/ButtonWithProgress';


class ResendConfirmationEmail extends Component {
    state = {
        id: this.props.loggedInUser.id,
        pendingApiCall: false,
        apiError: undefined,
        successfullyMessage: undefined,
        setButtonDisabled: false,
    };


    confirmationEmail = () => {
        this.setState({ pendingApiCall: true });
        apiCalls.resendEmailVerification(this.state.id, this.props.loggedInUser.jwt)
            .then((response) => {
                if (response.data.result === "FAIL") {
                    this.setState({
                        apiError: {
                            content: "Email already confirmed!"
                        },
                        setButtonDisabled: true,
                        pendingApiCall: false,
                    })
                } else {
                    this.setState({
                        pendingApiCall: false,
                        successfullyMessage: true,
                        setButtonDisabled: true,
                    })
                }
            })
            .catch((error) => {
                if (error.response) {
                    this.setState({
                        successfullyMessage: false,
                        apiError: error.response.data.message,
                        pendingApiCall: false,
                    });
                }
            });
    }

    render() {

        return (
            <div className="">

                <div className="container card d-flex p-1 card-confirmation shadow-sm">
                    <div className="alert text-center pb-0 mb-0" role="alert">
                        <div className="login-logo">
                            <img className="m-auto pl-3 pt-1" src={image} width="270" alt="Hoaxify" />
                        </div>

                        <div>
                            <i className="fas fa-envelope-open-text mail-icon"></i>
                            <span className="text-span font-weight-bold"> &nbsp;Confirm your email</span>
                        </div>

                        <h4 className="pt-2 confirmation-header">
                            Check Your Inbox!
                        </h4>

                        <p className="text-secondary pt-2 textConfirmation">
                            The confirmation of the email is necessary to have access to all the functionalities of
                            the application. A confirmation email has been sent to&nbsp;
                            <span className="font-weight-bold">{this.props.loggedInUser.email}</span>
                            &nbsp;at account creation. Please access the link inside it to confirm the email address.
                            To submit a new one, click the button below.
                        </p>

                        {this.state.successfullyMessage && (
                            <h5 className="text-success font-weight-bold pt-4 text-center text-resend">
                                <span className="far fa-check-circle fa-lg mb-1"></span>
                                <span className="">&nbsp;Email was successfully sent!</span>
                            </h5>
                        )}

                        {this.state.apiError && (
                            <h5 className="text-fail font-weight-bold pt-4 text-center text-resend">
                                <span className="far fa-times-circle fa-lg mb-1"></span>
                                <span className="">&nbsp;Email is already confirmed or the server is under maintenance!</span>
                                <br></br>
                                <span className="">&nbsp;Please refresh this page.</span>
                            </h5>
                        )}

                        <div className="text-center pt-4">
                            <ButtonWithProgress className="custom-row-email-button"
                                onClick={this.confirmationEmail}
                                disabled={this.state.setButtonDisabled}
                                pendingApiCall={this.state.pendingApiCall}
                                text="Resend Confirmation Email"
                            />
                        </div>

                        <p className="text-center display-7 text-secondary text-login-card-bottom pt-5">
                            For assistance, contact FitClub support at:
                            <br></br>
                            <ButtonMailto label="fitclub.by.alexnec@gmail.com" mailto="mailto:fitclub.by.alexnec@gmail.com" />
                        </p>
                    </div>
                </div>
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        loggedInUser: state
    }
}

export default connect(mapStateToProps)(ResendConfirmationEmail);