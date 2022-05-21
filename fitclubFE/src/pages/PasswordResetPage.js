import React, { useState, useEffect } from 'react';
import { Redirect } from 'react-router';
import queryString from 'query-string';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';
import { connect } from 'react-redux';
import ButtonWithProgress from '../components/ButtonWithProgress';
import Input from '../components/Input';
import TokenExpiredOrUsed from '../components/TokenExpiredOrUsed';
import Spinner from '../components/Spinner';

export const PasswordResetPage = (props) => {
    const [form, setForm] = useState({
        newPassword: "",
        newPasswordRepeat: "",
        tokenIdentifier: "tokenForPassword"
    });

    const [errors, setErrors] = useState({});
    const [pendingApiCall, setPendingApiCall] = useState(false);
    const [isLoadingToken, setIsLoadingToken] = useState(true);
    const [hasTokenExpired, setHasTokenExpired] = useState(true);
    const [successfullyMessage, setSuccessfullyMessage] = useState(false);
    const [redirect, setRedirect] = useState(false);
    const [shouldRedirect, setShouldRedirect] = useState(false);
    const token = (queryString.parse(props.location.search)).token;

    const onChange = (event) => {
        const { value, name } = event.target;
        setForm((previousForm) => {
            return {
                ...previousForm,
                [name]: value,
            };
        });

        setErrors((previousErrors) => {
            return {
                ...previousErrors,
                [name]: undefined,
            };
        });
    };


    useEffect(() => {
        const init = () => {
            apiCalls.checkValidToken(token, form.tokenIdentifier)
                .then((response) => {
                    if (response.data.result === "VALID") {
                        setHasTokenExpired(false);
                    }
                    setIsLoadingToken(false);
                })
                .catch((e) => {
                    setIsLoadingToken(false);
                    setHasTokenExpired(false);
                });
        };

        init();
    }, [token, form.tokenIdentifier]);

    useEffect(() => {
        if (shouldRedirect) {
            const id = setTimeout(() => {
                setRedirect(true);
            }, 5000);

            return () => clearTimeout(id);
        }
    }, [shouldRedirect]);

    const onClickSave = () => {
        const data = ({
            "newPassword": form.newPassword
        });
        setPendingApiCall(true);
        const action = {
            type: 'logout-success'
        };

        apiCalls.saveNewPassword(token, data)
            .then((response) => {
                console.log(data.newPassword);
                setPendingApiCall(false);
                setSuccessfullyMessage(true);
                if (passwordRepeatError === "") {
                    setShouldRedirect(true);
                }
            }, setTimeout(() => {
                props.dispatch(action);
            }, 1000))
            .catch((apiError) => {
                let newError = { ...errors };
                if (apiError.response.data && apiError.response.data.validationErrors) {
                    newError = { ...apiError.response.data.validationErrors };
                }
                setPendingApiCall(false);
                setErrors(newError);
            });
    };

    let passwordRepeatError;
    const { newPassword, newPasswordRepeat } = form;
    if (newPassword || newPasswordRepeat) {
        passwordRepeatError =
            newPassword === newPasswordRepeat ? "" : "Passwords must match!";
    }

    let pageContent;
    if (isLoadingToken) {
        pageContent = (
            <Spinner value="Loading..." />
        );
    } else if (!hasTokenExpired) {
        pageContent = (
            <div className="background-image pt-5" id="background-image">
                <div className="container p-5 pt-5">
                    <div className="containerSecurityChanges card d-flex shadow-sm mt-2">
                        <div className="alert pb-0 mb-0" role="alert">
                            <h4 className="pt-1 confirmation-header text-center">
                                Change Password
                            </h4>

                            <p className="text-center text-secondary pt-3 textConfirmation text-left">
                                If you would like to change/reset your password, enter a new password in the field below. Make sure both passwords are matching! <br></br>
                            </p>


                            <div className="col-12 mb-3">
                                <Input
                                    name="newPassword"
                                    label="New Password"
                                    placeholder="Your new password"
                                    type="password"
                                    value={form.newPassword}
                                    onChange={onChange}
                                    hasError={errors.newPassword && true}
                                    error={errors.newPassword}
                                />
                            </div>

                            <div className="col-12 mb-3">
                                <Input
                                    name="newPasswordRepeat"
                                    label="New Password Repeat"
                                    placeholder="Repeat your new password"
                                    type="password"
                                    value={form.newPasswordRepeat}
                                    onChange={onChange}
                                    hasError={passwordRepeatError && true}
                                    error={passwordRepeatError}
                                />
                            </div>

                            {successfullyMessage && (
                                <h5 className="text-success font-weight-bold pt-4 text-center text-resend">
                                    <span className="far fa-check-circle fa-lg mb-1"></span>
                                    <span className="">&nbsp;Password has been successfully changed!
                                        <br></br>In 5 seconds, you will be redirected to the Login.</span>
                                </h5>
                            )}

                            {!successfullyMessage && (
                                <div className="text-center mt-4">
                                    <ButtonWithProgress className="custom-row-edit-button"
                                        onClick={onClickSave}
                                        disabled={pendingApiCall || passwordRepeatError ? true : false}
                                        pendingApiCall={pendingApiCall}
                                        text="Change Password"
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
            </div>
        )
    } else {
        pageContent = (
            <TokenExpiredOrUsed />
        )
    }

    return redirect ? <Redirect to="/login" /> : <div>{pageContent}</div>
}

const mapDispatchToProps = dispatch => ({
    dispatch
})

export default connect(null, mapDispatchToProps)(PasswordResetPage)