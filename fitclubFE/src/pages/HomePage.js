import React from "react";
import UserList from "../components/UserList";
import MessageSubmit from "../components/MessageSubmit";
import { connect } from "react-redux";
import MessageFeed from "../components/MessageFeed";

class HomePage extends React.Component {
  render() {
    return (
      <div data-testid="homepage">
        <div className="row">
          <div className="col-8">
            {this.props.loggedInUser.isLoggedIn && <MessageSubmit />}
            <MessageFeed />
          </div>
          <div className="col-4">
            <UserList />
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(HomePage);
