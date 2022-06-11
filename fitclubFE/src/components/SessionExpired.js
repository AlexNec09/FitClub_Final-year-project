import React, { Component } from 'react'
import { connect } from 'react-redux';
import { BrowserRouter as Router } from 'react-router-dom';
import authImage from '../assets/authenticated.png';
import ButtonMailto from '../components/ButtonMailto';

class SessionExpired extends Component {

    render() {

        return (
            <Router>
                <div className="card d-flex p-1 shadow-sm">

                    <div className="alert text-center pt-4" role="alert">
                        <div className="login-logo">
                            <img className="m-auto pl-3" src={authImage} width="270" alt="UnauthorizedAccess" />
                        </div>
                        <h5 className="pt-2 text-unauthorized-access-header">
                            Unauthorized to see this content!
                        </h5>

                        <p className='text-unauthorized-access-body pt-5 text-black-100'>
                            Your session has expired.
                        </p>

                        <p className="text-secondary text-unauthorized-access-body">
                            <br />You need to&nbsp;
                            <a href="#/login" className="text-secondary text-black-50">
                                Login
                            </a>
                            &nbsp;again or contact FitClub support at:
                            <br></br>
                            <ButtonMailto label="fitclub.by.alexnec@gmail.com" mailto="mailto:fitclub.by.alexnec@gmail.com" />
                            <br></br>for assistance.
                        </p>
                    </div>
                </div>
            </Router>
        )
    }
}

const mapStateToProps = (state) => {
    return {
        loggedInUser: state
    }
}

export default connect(mapStateToProps)(SessionExpired);