import React, { useState, useEffect, useRef } from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import * as apiCalls from "../api/apiCalls";
import Spinner from "./Spinner";
import MessageView from "./MessageView";
import ModalView from "./ModalView";
import { connect } from "react-redux";
import AuthNeeded from "./AuthNeeded";
import SessionExpired from "./SessionExpired";
import ButtonWithProgress from "./ButtonWithProgress";
import Input from "./Input";
import securityAlert from '../assets/exclamationSecurity.png';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

const MessageFeed = (props) => {
  const [page, setPage] = useState({ content: [] });
  const [isLoadingMessages, setLoadingMessages] = useState(false);
  const [isClosedModal, setClosedModal] = useState();
  const [isLoadingOldMessages, setLoadingOldMessages] = useState(false);
  const [isLoadingNewMessages, setLoadingNewMessages] = useState(false);
  const [isDeletingMessage, setDeletingMessage] = useState(false);
  const [newMessageCount, setNewMessagesCount] = useState(0);
  const [messageToBeDeleted, setMessageToBeDeleted] = useState();
  // message submit
  const [focused, setFocused] = useState(false);
  const [content, setContent] = useState();
  const [pendingApiCall, setPendingApiCall] = useState(false);
  const [errors, setErrors] = useState({});
  const [image, setImage] = useState();
  const [attachment, setAttachment] = useState();

  const [hasFullAccess, setHasFullAccess] = useState(props.loggedInUser.isLoggedIn ? true : false);
  const intervalRef = useRef(null);

  useEffect(() => {
    const loadMessages = () => {
      if (hasFullAccess) {
        setLoadingMessages(true);
        apiCalls
          .loadMessages(props.user, props.loggedInUser.jwt)
          .then((response) => {
            setLoadingMessages(false);
            setPage(response.data);
          })
          .catch((error) => {
            setLoadingMessages(false);
          });
      }
    };
    loadMessages();
  }, [props.user, props.loggedInUser, hasFullAccess]);

  useEffect(() => {
    const checkCount = () => {
      const messages = page.content;
      let topMessageId = 0;
      if (messages.length > 0) {
        topMessageId = messages[0].id;
      }
      if (hasFullAccess) {
        apiCalls
          .loadNewMessagesCount(topMessageId, props.user, props.loggedInUser.jwt)
          .then((response) => {
            setNewMessagesCount(response.data.count);
          })
          .catch((error) => {
            setHasFullAccess(false);
          });;
      }
    };

    if (hasFullAccess) {
      intervalRef.current = setInterval(checkCount, 1500);
      // intervalRef.current = setInterval(checkCount, 50000);

      return function cleanup() {
        if (isLoadingNewMessages) {
          clearInterval(intervalRef.current);
          intervalRef.current = setInterval(checkCount, 50);
        }
        clearInterval(intervalRef.current);
      };
    }

  }, [props.user, page.content, isLoadingNewMessages, props.loggedInUser, hasFullAccess]);

  const onClickLoadMore = () => {
    if (isLoadingOldMessages) {
      return;
    }
    const messages = page.content;
    if (messages.length === 0) {
      return;
    }
    const messageAtBottom = messages[messages.length - 1];
    setLoadingOldMessages(true);
    apiCalls
      .loadOldMessages(messageAtBottom.id, props.user, props.loggedInUser.jwt)
      .then((response) => {
        setPage((previousPage) => ({
          ...previousPage,
          last: response.data.last,
          content: [...previousPage.content, ...response.data.content],
        }));
        setLoadingOldMessages(false);
      })
      .catch((error) => {
        setLoadingOldMessages(false);
      });
  };

  const onClickLoadNew = () => {
    if (isLoadingNewMessages) {
      return;
    }
    const messages = page.content;
    let topMessageId = 0;
    if (messages.length > 0) {
      topMessageId = messages[0].id;
    }
    setLoadingNewMessages(true);
    clearInterval(intervalRef.current);
    intervalRef.current = null;
    apiCalls
      .loadNewMessages(topMessageId, props.user, props.loggedInUser.jwt)
      .then((response) => {
        setPage((previousPage) => ({
          ...previousPage,
          content: [...response.data, ...previousPage.content],
        }));
        setLoadingNewMessages(false);
        setNewMessagesCount(0);
      })
      .catch((error) => {
        setLoadingNewMessages(false);
      });
  };

  const onClickModalOk = () => {
    setDeletingMessage(true);
    apiCalls.deleteMessage(messageToBeDeleted.id, props.loggedInUser.jwt).then((response) => {
      setPage((previousPage) => ({
        ...previousPage,
        content: previousPage.content.filter(
          (message) => message.id !== messageToBeDeleted.id
        ),
      }));
      setDeletingMessage(false);
      setMessageToBeDeleted();
      setClosedModal(false);
    });
  };

  const onClickModalCancel = () => {
    setMessageToBeDeleted();
    setClosedModal(false);
  };

  const onReact = (currentPost, reaction) => {
    apiCalls.messageReaction(currentPost.id, reaction, props.loggedInUser.jwt).then(res => {
      const messages = page.content.filter(message => {
        if (message.id !== currentPost.id) {
          return message;
        }

        const previousReaction = message.reactions.loggedUserReaction;

        if (previousReaction === 'LIKE') {
          message.reactions.likeCount -= 1;
        } else if (previousReaction === 'DISLIKE') {
          message.reactions.dislikeCount -= 1;
        }

        const newReaction = reaction.toUpperCase();
        if (previousReaction === newReaction) {
          message.reactions.loggedUserReaction = null;
        } else {
          message.reactions.loggedUserReaction = newReaction;
          if (newReaction === 'LIKE') {
            message.reactions.likeCount += 1;
          } else {
            message.reactions.dislikeCount += 1;
          }
        }
        return message;
      })
      setPage((previousPage) => ({
        ...previousPage,
        content: messages
      }));
    });
  }


  // Message Submit


  const onChangeContent = (event) => {
    setContent(event.target.value);
    setErrors({})
  };

  const onFileSelect = (event) => {
    if (event.target.files.length === 0) {
      return;
    }

    const file = event.target.files[0];
    let reader = new FileReader();
    reader.onloadend = () => {
      setImage(reader.result);
      uploadFile(event.target.files[0]);
    };
    reader.readAsDataURL(file);
  };

  const uploadFile = (receivedFile) => {
    const body = new FormData();
    body.append("file", receivedFile);
    apiCalls.postMessageFile(body, props.loggedInUser.jwt).then((response) => {
      setAttachment(response.data);
    });
  };

  const resetState = () => {
    setPendingApiCall(false);
    setFocused(false);
    setContent("");
    setErrors({});
    setImage();
    setAttachment();
  };

  const onClickSend = () => {
    const body = {
      content: content,
      attachment: attachment,
    };

    setPendingApiCall(true);

    apiCalls
      .postMessage(body, props.loggedInUser.jwt)
      .then((response) => {
        resetState();
      })
      .catch((error) => {
        let errors = {};
        if (error.response.data && error.response.data.validationErrors) {
          errors = error.response.data.validationErrors;
        }
        setPendingApiCall(false);
        setErrors(errors);
      });
  };

  const onFocus = () => {
    setFocused(true);
  };


  let textAreaClassName = "form-control w-100";
  if (errors.content) {
    textAreaClassName += " is-invalid";
  }

  if (isLoadingMessages) {
    return <Spinner />;
  }


  if (!hasFullAccess && !props.loggedInUser.isLoggedIn) {
    return <div className="pt-4">
      <AuthNeeded />
    </div>;
  }

  if (page.content.length === 0 && newMessageCount === 0 && hasFullAccess) {
    return (
      <div>
        <div className="pt-4">
          {(props.user == null || props.loggedInUser.username === props.user.username) && (<div className="pb-3">
            <div className="card d-flex flex-row p-2">
              <ProfileImageWithDefault
                className="rounded-circle m-1"
                width="32"
                height="32"
                image={props.loggedInUser.image}
              />
              <div className="flex-fill">
                <textarea
                  className={textAreaClassName}
                  placeholder="Share something with your followers"
                  rows={focused ? 3 : 1}
                  onFocus={onFocus}
                  value={content}
                  onChange={onChangeContent}
                />

                {errors.content && (
                  <span className="invalid-feedback">
                    {errors.content}
                  </span>
                )}

                {focused && (
                  <div>
                    <div className="pt-2">
                      <Input type="file" accept="image/png, image/jpeg" onChange={onFileSelect} />
                      {image && (
                        <img
                          className="mt-2 img-thumbnail"
                          src={image}
                          alt="uploadedImg"
                          width="128"
                          height="64"
                        />
                      )}
                    </div>
                    <div className="text-end mt-2">
                      <ButtonWithProgress
                        className="btn btn-success"
                        disabled={pendingApiCall}
                        onClick={onClickSend}
                        pendingApiCall={pendingApiCall}
                        text="Send"
                      />
                      <button
                        className="btn btn-light ms-1"
                        onClick={resetState}
                        disabled={pendingApiCall}
                      >
                        <i className="fas fa-times"></i> Cancel
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>)}
          <div className="card card-header text-center">There are no messages</div>
        </div>
      </div>
    );
  }
  const newMessageCountMessage =
    newMessageCount === 1
      ? "There is 1 new message"
      : `There are ${newMessageCount} new messages`;

  return (

    <div>
      <div className="pt-4">
        {(!hasFullAccess && props.loggedInUser.isLoggedIn) ? (<div className="card mb-3 verticalLineSecurity">
          <Row>
            <Col xs={11} md={11} lg={11} xl={11}>
              <div className="card-body d-flex flex-column ">
                <p className="text-secondary mb-0">
                  You need to be authenticated to access this resource!
                </p>
              </div>
            </Col>

            <Col xs={1} md={1} lg={1} xl={1}>
              <div className="d-flex justify-content-center securityMessageSubmit">
                <img className="m-auto" src={securityAlert} width="26" alt="SecurityAlert" />
              </div>
            </Col>

          </Row>
          <div className="pt-4">
            <SessionExpired />
          </div>
        </div>
        ) : (<div className="container pb-3">
          <div className="card d-flex flex-row p-2">
            <ProfileImageWithDefault
              className="rounded-circle m-1"
              width="32"
              height="32"
              image={props.loggedInUser.image}
            />
            <div className="flex-fill">
              <textarea
                className={textAreaClassName}
                placeholder="Share something with your followers"
                rows={focused ? 3 : 1}
                onFocus={onFocus}
                value={content}
                onChange={onChangeContent}
              />

              {errors.content && (
                <span className="invalid-feedback">
                  {errors.content}
                </span>
              )}

              {focused && (
                <div>
                  <div className="pt-2">
                    <Input type="file" accept="image/png, image/jpeg" onChange={onFileSelect} />
                    {image && (
                      <img
                        className="mt-2 img-thumbnail"
                        src={image}
                        alt="uploadedImg"
                        width="128"
                        height="64"
                      />
                    )}
                  </div>
                  <div className="text-end mt-2">
                    <ButtonWithProgress
                      className="btn btn-success"
                      disabled={pendingApiCall}
                      onClick={onClickSend}
                      pendingApiCall={pendingApiCall}
                      text="Send"
                    />
                    <button
                      className="btn btn-light ms-1"
                      onClick={resetState}
                      disabled={pendingApiCall}
                    >
                      <i className="fas fa-times"></i> Cancel
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
        )}
      </div>
      {(newMessageCount > 0 && props.loggedInUser.isLoggedIn) && (
        <div className="container">
          <div
            className="card card-header text-center"
            onClick={onClickLoadNew}
            style={{
              cursor: isLoadingNewMessages ? "not-allowed" : "pointer",
            }}
          >
            {isLoadingNewMessages ? <Spinner /> : newMessageCountMessage}
          </div>
        </div>
      )}
      {page.content.map((message) => {
        return (
          <div className="container pb-1">
            <MessageView
              key={message.id}
              message={message}
              onClickDelete={() => setMessageToBeDeleted(message)}
              onClickLike={() => onReact(message, 'like')}
              onClickDislike={() => onReact(message, 'dislike')}
            />
          </div>
        );
      })}
      {page.last === false && (
        <div className="container pb-3">
          <div
            className="card card-header text-center"
            onClick={onClickLoadMore}
            style={{
              cursor: isLoadingOldMessages ? "not-allowed" : "pointer",
            }}
          >
            {isLoadingOldMessages ? <Spinner /> : "View More Posts"}
          </div>
        </div>
      )}
      <ModalView
        visible={messageToBeDeleted && true}
        isClosed={isClosedModal}
        onClickCancel={onClickModalCancel}
        body={
          messageToBeDeleted &&
          "Are you sure you want to remove this message? This cannot be undone."
        }
        title="Delete message"
        okButton="Delete Post"
        onClickOk={onClickModalOk}
        pendingApiCall={isDeletingMessage}
      />
    </div>
  );
};


const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(MessageFeed);
