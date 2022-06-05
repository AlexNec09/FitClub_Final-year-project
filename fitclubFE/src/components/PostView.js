import React, { useRef } from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import { format } from "timeago.js";
import { Link } from "react-router-dom";
import { connect } from "react-redux";
import "./styles/DeleteButton.scss";
import "./styles/TimeSpan.scss";
import useClickTracker from "../shared/useClickTracker";

const PostView = (props) => {
  const actionArea = useRef();
  const dropDownVisible = useClickTracker(actionArea);
  const { post, onClickDelete, onClickLike, onClickDislike } = props;
  const { user, date, reactions } = post;
  const { username, displayName, image } = user;

  const relativeDate = format(date);
  const attachmentImageVisible =
    post.attachment && post.attachment.fileType.startsWith("image");

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
          width="40"
          height="40"
          image={image}
        />
        <div className="flex-fill m-auto ps-2">
          <div>
            <Link to={`/${username}`} className="custom-link-for-user">
              <h6 className="d-inline">
                {displayName}
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
                data-testid="post-actions-indicator"
                ref={actionArea}
              />
              <div
                className={dropDownClass}
                data-testid="post-action-dropdown"
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

      <div className="ps-5 pt-2">{post.content}</div>
      {attachmentImageVisible && (
        <div className="ps-5 pt-2">
          <img
            alt="attachment"
            src={`/images/attachments/${post.attachment.name}`}
            className="img-fluid"
          />
        </div>
      )}
      <div className="ps-5 pt-3 d-flex">
        <div className={reactions && reactions.loggedUserReaction === "LIKE" ? "text-success w-25" : "text-muted w-25"} data-testid="like-reaction" style={{ cursor: 'pointer' }} onClick={onClickLike}>
          <i className="fas fa-thumbs-up"></i><span className="ps-1">{reactions && reactions.likeCount}</span>
        </div>
        <div className={reactions && reactions.loggedUserReaction === "DISLIKE" ? "text-danger w-25" : "text-muted w-25"} data-testid="dislike-reaction" style={{ cursor: 'pointer' }} onClick={onClickDislike}>
          <i className="fas fa-thumbs-down"></i><span className="ps-1">{reactions && reactions.dislikeCount}</span>
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

export default connect(mapStateToProps)(PostView);
