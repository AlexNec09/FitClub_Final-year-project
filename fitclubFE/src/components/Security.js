import React, { Component } from 'react'
import { connect } from 'react-redux';
// import ButtonSecurityChangePassword from './ButtonSecurityChangePassword';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
// import { Link } from 'react-router-dom';
import exclamationSecurity from '../assets/exclamationSecurity.png';
import * as apiCalls from '../api/apiCalls';

class Security extends Component {
    state = {
        id: this.props.loggedInUser.id,

        // // Email
        // pendingApiCallChangeEmail: false,
        // successfullyMessageChangeEmail: false,
        // setButtonDisabledChangeEmail: false,
        // apiErrorChangeEmail: false,
        // successfullyEmailMessage: false,

        // // Password
        // pendingApiCallChangePassword: false,

    };

    render() {

        return (
            <React.Fragment>

                <div className="card mt-4 verticalLine">
                    <Row>
                        <Col xs={11} md={11} lg={11} xl={10}>
                            <div className="card-body d-flex flex-column ">

                                <p className="text-secondary textSecurityTop mb-0">
                                    Many of the services offered use the security method to confirm the&nbsp;

                                    <span className="font-weight-bold mb-0">e-mail account holder. </span>

                                    <br></br>
                                    Please inform yourself before a useful service.&nbsp;
                                </p>

                            </div>
                        </Col>

                        <Col xs={1} md={1} lg={1} xl={2}>
                            <div className="d-flex justify-content-center exclamationSecurity pt-2">
                                <img className="m-auto" src={exclamationSecurity} width="32" alt="Hoaxify" />
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