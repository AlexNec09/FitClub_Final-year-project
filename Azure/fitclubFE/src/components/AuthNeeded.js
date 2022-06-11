import React, { Component } from 'react'
import { connect } from 'react-redux';
import authImage from '../assets/authenticated.png';
import ButtonMailto from '../components/ButtonMailto';

class AuthNeeded extends Component {

    render() {

        return (
            <div className="card d-flex p-1 shadow-sm">

                <div className="alert text-center pt-4" role="alert">
                    <div className="login-logo">
                        <img className="m-auto pl-3" src={authImage} width="270" alt="UnauthorizedAccess" />
                    </div>
                    {/* <i className="fas fa-exclamation-triangle fa-5x icon-exclamation" /> */}
                    <h5 className="pt-2 text-unauthorized-access-header">
                        Unauthorized to see this content!
                    </h5>

                    <p className='text-unauthorized-access-body pt-5 text-black-100'>
                        Authentication is required to see this content.
                    </p>

                    <p className="text-secondary text-unauthorized-access-body">
                        <br />You need to&nbsp;
                        <a href="#/login" className="text-secondary text-black-50">
                            Login
                        </a>
                        &nbsp;or&nbsp;
                        <a href="#/signup" className="text-secondary text-black-50">
                            Sign Up
                        </a>
                        .
                    </p>

                    <p className="text-center text-secondary text-login-card-bottom pt-5">
                        For assistance, contact FitClub support at:
                        <br></br>
                        <ButtonMailto label="fitclub.by.alexnec@gmail.com" mailto="mailto:fitclub.by.alexnec@gmail.com" />
                    </p>
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

export default connect(mapStateToProps)(AuthNeeded);