import React, { Component } from 'react'
import ButtonMailto from '../components/ButtonMailto';
import ButtonWithProgressForEmails from './ButtonWithProgressForEmails';
import ButtonWithProgress from './ButtonWithProgress';
import exclamation_icon from '../assets/exclamation-icon.png';
import { Link } from 'react-router-dom';

class TokenExpiredOrUsed extends Component {

    render() {

        return (
            <div className="container card d-flex p-1 card-confirmation shadow-sm">
                <div className="alert text-center pb-0 mb-0" role="alert">
                    <div className="">
                        <img className="m-auto pl-3 pt-3" src={exclamation_icon} width="200" alt="SecurityAlert" />
                    </div>

                    <h4 className="pt-5 confirmation-header">
                        Something went wrong!
                    </h4>

                    <p className="text-secondary pt-4 textConfirmation">
                        The token is no longer available or the server is under maintenance.
                    </p>

                    <div className="container text-center pt-4">
                        <Link to="/" className="list-group-item-action">
                            <ButtonWithProgress className="custom-row-edit-button"
                                text="Back to Home Page"
                            />
                        </Link>
                    </div>

                    <p className="text-center display-7 text-secondary text-login-card-bottom pt-5">
                        For assistance, contact FitClub support at:
                        <br></br>
                        <ButtonMailto label="fitclub.by.alexnec@gmail.com" mailto="mailto:fitclub.by.alexnec@gmail.com" />
                    </p>
                </div>

            </div>
        )
    }
}

export default TokenExpiredOrUsed;