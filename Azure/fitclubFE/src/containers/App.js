import React, { Component } from 'react'
import { Route, Switch, Redirect } from 'react-router-dom';
import HomePage from "../pages/HomePage";
import LoginPage from "../pages/LoginPage";
import UserPage from "../pages/UserPage";
import UserSignupPage from "../pages/UserSignupPage";
import TopBar from "../components/TopBar";
import './App.css';
import { connect } from 'react-redux';
import ResendConfirmationEmail from '../pages/ResendConfirmationEmail';
import ConfirmationToken from '../pages/ConfirmationToken';
import ChangeEmailPage from '../pages/ChangeEmailPage';
import PasswordResetPage from '../pages/PasswordResetPage';
import RecoverPasswordPage from '../pages/RecoverPasswordPage';

class App extends Component {
  render() {
    return (
      <div className='wrapper'>
        <TopBar />
        <div>
          <Switch>
            <Route path="/:verification/confirmationToken" component={ConfirmationToken} />

            {this.props.loggedInUser.emailVerificationStatus === true || this.props.loggedInUser.emailVerificationStatus === undefined ? <Route exact path="/" component={HomePage} />
              : <Route exact path="/verification/confirmationEmail" component={ResendConfirmationEmail} />}
            {!this.props.loggedInUser.emailVerificationStatus && (<Route exact path="/" component={ResendConfirmationEmail} />)}
            <Route exact path="/forgotPassword" component={RecoverPasswordPage} />
            {!this.props.loggedInUser.isTokenValid && (this.props.loggedInUser.emailVerificationStatus === true || this.props.loggedInUser.emailVerificationStatus === undefined) && <Route exact path="/login" component={LoginPage} />}
            <Route exact path="/verification/changeEmail" component={ChangeEmailPage} />
            <Route exact path="/verification/passwordReset" component={PasswordResetPage} />

            {this.props.loggedInUser.emailVerificationStatus === true || this.props.loggedInUser.emailVerificationStatus === undefined ? <Route exact path="/users/:username" component={UserPage} /> : <Redirect to="/" />}
            <Redirect from="/verification/confirmationEmail" exact to="/" />
            {!this.props.loggedInUser.isLoggedIn ? <Route exact path="/signup" component={UserSignupPage} /> : <Redirect to="/" />}
            <Route component={HomePage} />

          </Switch>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state
  }
}

export default connect(mapStateToProps)(App);