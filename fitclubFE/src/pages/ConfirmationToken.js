import React, { Component } from 'react';
import queryString from 'query-string';
import { Redirect } from 'react-router';
import * as apiCalls from '../api/apiCalls';
import Spinner from '../components/Spinner';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import yes_icon from '../assets/yes-icon.jpg';
import ButtonWithProgressForEmails from '../components/ButtonWithProgressForEmails';
import TokenExpiredOrUsed from '../components/TokenExpiredOrUsed';


class ConfirmationToken extends Component {
    state = {
        token: this.props.match.params.token,
        isLoadingToken: false,
        error: false,
    }

    componentDidMount() {
        let url = this.props.location.search;
        let params = queryString.parse(url);

        this.setState({
            token: params.token,
            isLoadingToken: true,

            isLoadingResponse: false,
            response: null,
            apiError: null
        })

        apiCalls.confirmationToken(params.token)
            .then((response) => {
                this.setState({
                    isLoadingToken: false,
                    error: false
                }, () => {
                    const action = {
                        type: 'confirmation-token'
                    };
                    this.props.dispatch(action);
                })
                this.id = setTimeout(() => this.setState({ redirect: true }), 5000)
            })
            .catch((e) => {
                this.setState({
                    isLoadingToken: false,
                    error: true
                });
            })

    };

    componentWillUnmount() {
        clearTimeout(this.id)
    }

    render() {
        let pageContent;
        if (this.state.isLoadingToken) {
            pageContent = (
                <Spinner value="Loading..." />
            );
        } else if (this.state.error === false) {
            pageContent = (
                <div>
                    <div className="container card d-flex p-1 card-confirmation shadow-sm">
                        <div className="alert text-center pb-0 mb-0" role="alert">
                            <div className="">
                                <img className="m-auto pl-3 pt-3" src={yes_icon} width="200" alt="Hoaxify" />
                            </div>

                            <h4 className="pt-5 confirmation-header">
                                Your email has been successfully confirmed.
                            </h4>

                            <p className="text-secondary pt-4 textConfirmation">

                                The changes were successful, please check the Email Verification Status in
                                "My Profile" to make sure the change.
                                <br></br>
                                In 5 seconds you will be redirected to the Home Page.
                                <span className="font-weight-bold"></span>

                            </p>

                            {this.state.successfullyMessage && (

                                <h5 className="text-success font-weight-bold pt-3 text-center success-text-resend">
                                    <span className="far fa-check-circle fa-lg fa-2x"></span>
                                    <span className="">&nbsp;Email Resending was successfully!</span>
                                </h5>
                            )}

                            <div className="text-center pt-4 mb-4">
                                <Link to="/" className="list-group-item-action">
                                    <ButtonWithProgressForEmails
                                        value="Back to Home Page &nbsp;&nbsp;"
                                    />
                                </Link>
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
        return this.state.redirect ? <Redirect to="/" /> : <div>{pageContent}</div>
    }
}

const mapStateToProps = (state) => {
    return {
        user: state
    };
};

export default connect(mapStateToProps)(ConfirmationToken);