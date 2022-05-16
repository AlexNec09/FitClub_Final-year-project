import React, { useState, useEffect } from 'react';
import { Redirect } from 'react-router';
import queryString from 'query-string';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';
import { connect } from 'react-redux';
import ButtonWithProgressForEmails from '../components/ButtonWithProgressForEmails';
import Input from '../components/Input';
import TokenExpiredOrUsed from '../components/TokenExpiredOrUsed';
import Spinner from '../components/Spinner';


export const ChangeEmailPage = (props) => {
    const [form, setForm] = useState({
        newEmail: "",
        newEmailRepeat: "",
        tokenIdentifier: "tokenForEmail"
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
            "newEmail": form.newEmail
        });
        setPendingApiCall(true);
        const action = {
            type: 'logout-success'
        };

        apiCalls.saveChangeEmail(token, data)
            .then((response) => {
                setPendingApiCall(false);
                setSuccessfullyMessage(true);
                if (emailRepeatError === "") {
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

    let emailRepeatError;
    const { newEmail, newEmailRepeat } = form;
    if (newEmail || newEmailRepeat) {
        emailRepeatError =
            newEmail === newEmailRepeat ? "" : "Email addresses must match!";
    }

    let pageContent;
    if (isLoadingToken) {
        pageContent = (
            <Spinner value="Loading..." />
        );
    } else if (!hasTokenExpired) {
        pageContent = (
            <div className="">
                <div className="containerSecurityChanges card d-flex shadow-sm mt-2">
                    <div className="alert pb-0 mb-0" role="alert">
                        <h4 className="pt-1 confirmation-header text-center">
                            Change Email
                        </h4>

                        <p className="text-secondary pt-3 textConfirmation text-left">
                            If you would like to change your email, enter a new email in the field below.
                            Before being able to log back in, you will have to verify your new address by
                            clicking the activation link in the email we send to your new address.&nbsp;
                        </p>



                        <div className="col-12 mb-3">
                            <Input
                                name="newEmail"
                                label="New Email Address"
                                placeholder="Your new email address"
                                type="email"
                                value={form.newEmail}
                                onChange={onChange}
                                hasError={errors.newEmail && true}
                                error={errors.newEmail}
                            />
                        </div>


                        <div className="col-12 mb-3">
                            <Input
                                name="newEmailRepeat"
                                label="New Email Address Repeat"
                                placeholder="Repeat your new email address"
                                type="email"
                                value={form.newEmailRepeat}
                                onChange={onChange}
                                hasError={emailRepeatError && true}
                                error={emailRepeatError}
                            />
                        </div>

                        {successfullyMessage && (
                            <h5 className="text-success font-weight-bold pt-3 text-center text-resend">
                                <span className="far fa-check-circle fa-lg mb-1"></span>
                                <span className="">&nbsp;Email has been successfully changed!
                                    <br></br>In 5 seconds, you will be redirected to the Login.</span>
                            </h5>
                        )}

                        {!successfullyMessage && (
                            <div className="text-center mt-3">
                                <ButtonWithProgressForEmails
                                    onClick={onClickSave}
                                    disabled={pendingApiCall || emailRepeatError ? true : false}
                                    pendingApiCall={pendingApiCall}
                                    value="Save&nbsp;"
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

    return redirect ? <Redirect to="/login" /> : <div>{pageContent}</div>
}

const mapDispatchToProps = dispatch => ({
    dispatch
})

export default connect(null, mapDispatchToProps)(ChangeEmailPage)