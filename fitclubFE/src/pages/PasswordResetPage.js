import React, { Component } from 'react';
import { Redirect } from 'react-router';
import queryString from 'query-string';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';
import { connect } from 'react-redux';
import ButtonWithProgressForEmails from '../components/ButtonWithProgressForEmails';
import Input from '../components/Input';
import TokenExpiredOrUsed from '../components/TokenExpiredOrUsed';
import Spinner from '../components/Spinner';

class PasswordResetPage extends Component {
    state = {
        token: this.props.match.params.token,
        redirect: false,
        password: '',
        passwordRepeat: '',
        hasTokenExpired: true,
        isLoadingToken: true,
        errors: {},
        tokenIdentifier: "tokenForPassword",
        successfullyMessage: false,
        passwordRepeatConfirmed: false,
    }

    componentDidMount() {
        let url = this.props.location.search;
        let params = queryString.parse(url);

        this.setState({
            token: params.token,
        })

        console.log("THE TOKEN: " + params.token);

        apiCalls.checkValidToken(params.token, this.state.tokenIdentifier)
            .then((response) => {
                console.log("FROM PASSWORD: " + JSON.stringify(response));
                if (response.data.result === "VALID") {
                    this.setState({
                        hasTokenExpired: false
                    });
                }
                this.setState({
                    isLoadingToken: false,
                });
            })
            .catch((e) => {
                this.setState({
                    isLoadingToken: false,
                    hasTokenExpired: false
                });
            })

    };

    componentWillUnmount() {
        clearTimeout(this.id)
    }

    onChangePassword = (event) => {
        const value = event.target.value;
        const passwordRepeatConfirmed = this.state.passwordRepeat === value;
        const errors = { ...this.state.errors };
        delete errors.newpassword;
        errors.passwordRepeat = passwordRepeatConfirmed ? '' : 'Passwords do not match!'
        this.setState({ password: value, passwordRepeatConfirmed, errors });
    };

    onChangePasswordRepeat = (event) => {
        const value = event.target.value;
        const passwordRepeatConfirmed = this.state.password === value;
        const errors = { ...this.state.errors };
        errors.passwordRepeat = passwordRepeatConfirmed ? '' : 'Passwords do not match!'
        this.setState({ passwordRepeat: value, passwordRepeatConfirmed, errors });
    };

    onClickSave = () => {
        const data = ({
            "newPassword": this.state.password
        });
        this.setState({ pendingApiCall: true });
        const action = {
            type: 'logout-success'
        };

        apiCalls.saveNewPassword(this.state.token, data)
            .then((response) => {
                this.setState({
                    pendingApiCall: false,
                    successfullyMessage: true,
                })
                this.id = setTimeout(() => this.setState({ redirect: true }), 5000)
            }, setTimeout(() => this.props.dispatch(action), 1000))

            .catch((apiError) => {
                let newError = { ...this.state.errors };
                if (apiError.response.data && apiError.response.data.validationErrors) {
                    newError = { ...apiError.response.data.validationErrors };
                }
                this.setState({
                    pendingApiCall: false,
                    errors: newError
                });
            });


    };

    handleSubmit = (event) => {
        event.preventDefault();
        this.props.history.push('/');
    }

    render() {

        let pageContent;
        if (this.state.isLoadingToken) {
            pageContent = (
                <Spinner value="Loading..." />
            );
        } else if (!this.state.hasTokenExpired) {
            pageContent = (
                <div className="">
                    <div className="containerSecurityChanges card d-flex shadow-sm mt-2">
                        <div className="alert pb-0 mb-0" role="alert">
                            <h4 className="pt-1 confirmation-header text-center">
                                Change Password
                            </h4>

                            <p className="text-secondary pt-3 textConfirmation text-left">
                                If you would like to change your password, enter a new password in the field below.
                                Before being able to log back in, you will have to verify your new address by
                                clicking the activation link in the password we send to your new address.&nbsp;
                            </p>
                            <div className="form-group py-4 pt-5">
                                <Input
                                    label="New Password"
                                    placeholder="New Password"
                                    value={this.state.password}
                                    onChange={this.onChangePassword}
                                    hasError={this.state.errors.newPassword && true}
                                    error={this.state.errors.newPassword}
                                />
                            </div>

                            <div className="form-group py-4">
                                <Input
                                    label="New Password Repeat"
                                    placeholder="Repeat your new password"
                                    value={this.state.passwordRepeat}
                                    onChange={this.onChangepasswordRepeat}
                                    hasError={this.state.errors.newPassword && true}
                                    error={this.state.errors.newPassword}
                                />
                            </div>

                            {this.state.successfullyMessage && (
                                <h5 className="text-success font-weight-bold pt-3 text-center text-resend">
                                    <span className="far fa-check-circle fa-lg mb-1"></span>
                                    <span className="">&nbsp;Password has been successfully changed!
                                        <br></br>In 5 seconds, you will be redirected to the Login.</span>
                                </h5>
                            )}

                            {!this.state.successfullyMessage && (
                                <div className="pull-right pt-3">
                                    <ButtonWithProgressForEmails
                                        onClick={this.onClickSave}
                                        disabled={this.state.pendingApiCall || !this.state.passwordRepeatConfirmed}
                                        pendingApiCall={this.state.pendingApiCall}
                                        text="Save"
                                    />
                                </div>
                            )}

                            <p className="text-center display-7 text-secondary text-login-card-bottom pt-5">
                                For assistance, contact FitClub support at:
                                <br></br>
                                <ButtonMailto label="fitclub.by.alexnec@gmail.com" mailto="mailto:fitclub.by.alexnec@gmail.com" />
                            </p>
                        </div>
                    </div>
                </div>
            )
        } else {
            pageContent = (
                <TokenExpiredOrUsed />
            )
        }
        return this.state.redirect ? <Redirect to="/login" /> : <div>{pageContent}</div>
    }
}

const mapStateToProps = (state) => {
    return {
        user: state
    };
};

export default connect(mapStateToProps)(PasswordResetPage);