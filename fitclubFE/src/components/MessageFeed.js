import React, { useState, useEffect, useRef } from "react";
import * as apiCalls from "../api/apiCalls";
import Spinner from "./Spinner";
import MessageView from "./MessageView";
import ModalView from "./ModalView";

const MessageFeed = (props) => {
  const [page, setPage] = useState({ content: [] });
  const [isLoadingMessages, setLoadingMessages] = useState(false);
  const [isClosedModal, setClosedModal] = useState();
  const [isLoadingOldMessages, setLoadingOldMessages] = useState(false);
  const [isLoadingNewMessages, setLoadingNewMessages] = useState(false);
  const [isDeletingMessage, setDeletingMessage] = useState(false);
  const [newMessageCount, setNewMessagesCount] = useState(0);
  const [messageToBeDeleted, setMessageToBeDeleted] = useState();
  const intervalRef = useRef(null);

  useEffect(() => {
    const loadMessages = () => {
      setLoadingMessages(true);
      apiCalls
        .loadMessages(props.user)
        .then((response) => {
          setLoadingMessages(false);
          setPage(response.data);
        })
        .catch((error) => {
          setLoadingMessages(false);
        });
    };
    loadMessages();
  }, [props.user]);

  useEffect(() => {
    const checkCount = () => {
      const messages = page.content;
      let topMessageId = 0;
      if (messages.length > 0) {
        topMessageId = messages[0].id;
      }
      apiCalls
        .loadNewMessagesCount(topMessageId, props.user)
        .then((response) => {
          setNewMessagesCount(response.data.count);
        });
    };
    intervalRef.current = setInterval(checkCount, 2500);
    return function cleanup() {
      if (isLoadingNewMessages) {
        clearInterval(intervalRef.current);
        intervalRef.current = setInterval(checkCount, 50);
      }
      clearInterval(intervalRef.current);
    };
  }, [props.user, page.content, isLoadingNewMessages]);

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
      .loadOldMessages(messageAtBottom.id, props.user)
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
      .loadNewMessages(topMessageId, props.user)
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
    apiCalls.deleteMessage(messageToBeDeleted.id).then((response) => {
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
    apiCalls.messageReaction(currentPost.id, reaction).then(res => {
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

  if (isLoadingMessages) {
    return <Spinner />;
  }
  if (page.content.length === 0 && newMessageCount === 0) {
    return (
      <div className="card card-header text-center">There are no messages</div>
    );
  }
  const newMessageCountMessage =
    newMessageCount === 1
      ? "There is 1 new message"
      : `There are ${newMessageCount} new messages`;
  return (
    <div>
      {newMessageCount > 0 && (
        <div
          className="card card-header text-center"
          onClick={onClickLoadNew}
          style={{
            cursor: isLoadingNewMessages ? "not-allowed" : "pointer",
          }}
        >
          {isLoadingNewMessages ? <Spinner /> : newMessageCountMessage}
        </div>
      )}
      {page.content.map((message) => {
        return (
          <MessageView
            key={message.id}
            message={message}
            onClickDelete={() => setMessageToBeDeleted(message)}
            onClickLike={() => onReact(message, 'like')}
            onClickDislike={() => onReact(message, 'dislike')}
          />
        );
      })}
      {page.last === false && (
        <div
          className="card card-header text-center"
          onClick={onClickLoadMore}
          style={{
            cursor: isLoadingOldMessages ? "not-allowed" : "pointer",
          }}
        >
          {isLoadingOldMessages ? <Spinner /> : "View More Posts"}
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

export default MessageFeed;
