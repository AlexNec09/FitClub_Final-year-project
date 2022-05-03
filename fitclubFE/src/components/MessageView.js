import React, { useRef } from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import { format } from "timeago.js";
import { Link } from "react-router-dom";
import { connect } from "react-redux";
import "./styles/DeleteButton.scss";
import "./styles/TimeSpan.scss";
import useClickTracker from "../shared/useClickTracker";

const MessageView = (props) => {
  const actionArea = useRef();
  const dropDownVisible = useClickTracker(actionArea);
  const { message, onClickDelete, onClickLike, onClickDislike } = props;
  const { user, date, reactions } = message;
  const { username, displayName, image } = user;

  const relativeDate = format(date);
  const attachmentImageVisible =
    message.attachment && message.attachment.fileType.startsWith("image");

  const ownedByLoggedInUser = user.id === props.loggedInUser.id;

  let dropDownClass = "p-0 shadow dropdown-menu";
  if (dropDownVisible) {
    dropDownClass += " show";
  }

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
            <div className="dropdown">
              <span
                className="btn btn-sm btn-light dropdown-toggle"
                data-testid="message-actions-indicator"
                ref={actionArea}
              />
              <div
                className={dropDownClass}
                data-testid="message-action-dropdown"
              >
                <button
                  className="btn btn-outline-danger btn-sm w-100 d-block"
                  onClick={onClickDelete}
                >
                  <i className="far fa-trash-alt" /> Delete
                </button>
              </div>
            </div>
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
      <div className="ps-5 pt-3 d-flex">
        <div className={reactions && reactions.loggedUserReaction === "LIKE" ? "text-success w-25" : "text-muted w-25"} data-testid="like-reaction" style={{ cursor: 'pointer' }} onClick={onClickLike}>
          <i className="far fa-thumbs-up"></i><span className="ps-1">{reactions && reactions.likeCount}</span>
        </div>
        <div className={reactions && reactions.loggedUserReaction === "DISLIKE" ? "text-danger w-25" : "text-muted w-25"} data-testid="dislike-reaction" style={{ cursor: 'pointer' }} onClick={onClickDislike}>
          <i className="far fa-thumbs-down"></i><span className="ps-1">{reactions && reactions.dislikeCount}</span>
        </div>
      </div>
    </div>
  );
};

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(MessageView);
