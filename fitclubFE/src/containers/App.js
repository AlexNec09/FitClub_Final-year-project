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
            <Route exact path="/" component={ResendConfirmationEmail} />
            {this.props.loggedInUser.emailVerificationStatus === true || this.props.loggedInUser.emailVerificationStatus === undefined ? <Route exact path="/login" component={LoginPage} /> : <Redirect to="/" />}
            <Route exact path="/verification/changeEmail" component={ChangeEmailPage} />
            <Route exact path="/verification/passwordReset" component={PasswordResetPage} />

            {this.props.loggedInUser.isLoggedIn === false && <Route exact path="/signup" component={UserSignupPage} />}
            {this.props.loggedInUser.emailVerificationStatus === true || this.props.loggedInUser.emailVerificationStatus === undefined ? <Route exact path="/:username" component={UserPage} /> : <Redirect to="/" />}
            <Redirect from="/verification/confirmationEmail" exact to="/" />
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