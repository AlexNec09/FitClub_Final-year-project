import React, { Component } from 'react'
import { connect } from 'react-redux';
import image from '../assets/confirmationEmail.png';
import ButtonWithProgressEmailConfirmation from '../components/ButtonWithProgressEmailConfirmation';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';

class ResendConfirmationEmail extends Component {
    state = {
        id: this.props.loggedInUser.id,
        pendingApiCall: false,
        apiError: undefined,
        successfullyMessage: false,
        setButtonDisabled: false,
    };

    confirmationEmail = () => {
        this.setState({ pendingApiCall: true });
        apiCalls.resendEmailVerification(this.state.id, this.props.loggedInUser.jwt)
            .then((response) => {
                this.setState({
                    pendingApiCall: false,
                    successfullyMessage: true,
                    setButtonDisabled: true,
                })
            })
            .catch((error) => {
                if (error.response) {
                    this.setState({
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
                            <span className="font-weight-bold">{this.props.loggedInUser.username}</span>
                            &nbsp;at account creation. Please access the link inside it to confirm the email address.
                            To submit a new one, click the button below.
                        </p>

                        {this.state.successfullyMessage && (

                            <h5 className="text-success font-weight-bold pt-3 text-center success-text-resend">
                                <span className="far fa-check-circle fa-lg fa-2x"></span>
                                <span className="">&nbsp;Email was successfully sent!</span>
                            </h5>
                        )}

                        <div className="text-center pt-4">
                            <ButtonWithProgressEmailConfirmation
                                onClick={this.confirmationEmail}
                                // disabled={disableSubmit || this.state.pendingApiCall}
                                disabled={this.state.setButtonDisabled}
                                pendingApiCall={this.state.pendingApiCall}
                                value="Resend Confirmation Email &nbsp;&nbsp;"
                            />
                        </div>

                        <p className="text-center display-6 text-secondary text-login-card-bottom pt-5">
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