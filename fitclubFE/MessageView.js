import React, { Component } from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import { format } from "timeago.js";
import { Link } from "react-router-dom";
import { connect } from "react-redux";
import "./styles/DeleteButton.scss";
import "./styles/TimeSpan.scss";

class MessageView extends Component {
  render() {
    const { message, onClickDelete } = this.props;
    const { user, date } = message;
    const { username, displayName, image } = user;

    const relativeDate = format(date);
    const attachmentImageVisible =
      message.attachment && message.attachment.fileType.startsWith("image");

    const ownedByLoggedInUser = user.id === this.props.loggedInUser.id;

    return (
      <div className="card p-2">
        <div className="d-flex">
          <ProfileImageWithDefault
            className="rounded-circle m-1"
            width="32"
            height="32"
            image={image}
          />
          <div className="flex-fill m-auto ps-2">
            <div>
              <Link to={`/${username}`} className="list-group-item-action">
                <h6 className="d-inline">
                  {displayName}@{username}
                </h6>
              </Link>
              <span className="brsmaller"></span>
              <span className="text-black-50 fs-7">{relativeDate}</span>
            </div>
          </div>
          <div className="d-flex align-items-center pt-1">
            {ownedByLoggedInUser && (
              <button
                className="btn btn-outline-danger btn-sm btn-block text-left"
                onClick={onClickDelete}
              >
                <i className="far fa-trash-alt" />
              </button>
            )}
          </div>
        </div>

        <div className="ps-5 pt-2">{message.content}</div>
        {attachmentImageVisible && (
          <div className="ps-5 pt-2">
            <img
              alt="attachment"
              src={`/images/attachments/${message.attachment.name}`}
              className="img-fluid"
            />
          </div>
        )}
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(MessageView);
