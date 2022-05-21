import React, { useState } from 'react';
import * as apiCalls from '../api/apiCalls';
import ButtonMailto from '../components/ButtonMailto';
import ButtonWithProgress from '../components/ButtonWithProgress';
import Input from '../components/Input';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import exclamationSecurity from '../assets/exclamationSecurity.png';

export const RecoverPasswordPage = () => {
    const [form, setForm] = useState({
        newEmail: "",
    });

    const [errors, setErrors] = useState({});
    const [searchError, setSearchError] = useState();
    const [pendingApiCall, setPendingApiCall] = useState(false);
    const [successfullyMessage, setSuccessfullyMessage] = useState(false);

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

    const onClickSearch = () => {
        const data = ({
            "newEmail": form.newEmail
        });
        setPendingApiCall(true);

        apiCalls.recoverPasswordByEmail(data)
            .then((response) => {
                if (response.status !== 200) {
                    setSuccessfullyMessage(false);
                    setSearchError("Your search did not return any account. Please try again.")
                } else {
                    setSuccessfullyMessage(true);
                    setSearchError();
                }
                setPendingApiCall(false);
            })
            .catch((apiError) => {
                let newError = { ...errors };
                if (apiError.response.data && apiError.response.data.validationErrors) {
                    newError = { ...apiError.response.data.validationErrors };
                }
                setPendingApiCall(false);
                setErrors(newError);
            });
    };

    let pageContent = (
        <div className="background-image pt-5" id="background-image">
            <div className="container p-5 pt-5">
                <div className="containerSecurityChanges card d-flex shadow-sm mt-2">
                    <div className="alert pb-0 mb-0" role="alert">
                        <h4 className="pt-1 confirmation-header text-center">
                            Recover Password
                        </h4>

                        <p className="text-center text-secondary pt-3 textConfirmation text-left">
                            Please enter your email address to search for your account on FitClub platform. <br></br>
                        </p>
                        {searchError && (<div className="card mt-5 mb-4 notFoundAddressLine">
                            <div className="card-body d-flex flex-column ">
                                <p className="notFoundAddressText mb-0">
                                    {searchError}
                                </p>
                            </div>
                        </div>)}



                        <div className="col-12 mb-3">
                            <Input
                                name="newEmail"
                                label="Email Address"
                                placeholder="Your email address"
                                type="email"
                                value={form.newEmail}
                                onChange={onChange}
                                hasError={errors.newEmail && true}
                                error={errors.newEmail}
                                readOnly={successfullyMessage}
                            />
                        </div>

                        {successfullyMessage && (
                            <h5 className="text-success font-weight-bold pt-4 text-center text-resend">
                                <span className="far fa-check-circle fa-lg mb-1"></span>
                                <span className="">&nbsp;Email has been successfully sent!
                                    <br></br>Please follow the instructions in the email.</span>
                            </h5>
                        )}

                        {!successfullyMessage && (
                            <div className="container text-center mt-4">
                                <ButtonWithProgress className="custom-row-edit-button"
                                    onClick={onClickSearch}
                                    disabled={pendingApiCall}
                                    pendingApiCall={pendingApiCall}
                                    text="Search"
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
    );

    return <div>{pageContent}</div>
}

export default RecoverPasswordPage;