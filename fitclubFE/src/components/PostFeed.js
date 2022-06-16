import React, { useEffect, useState, useRef } from "react";
import * as apiCalls from "../api/apiCalls";
import Spinner from "./Spinner";
import PostView from "./PostView";
import PostSubmit from "./PostSubmit";
import ModalView from "./ModalView";
import { connect, useDispatch } from "react-redux";
import AuthNeeded from "./AuthNeeded";
import SessionExpired from "./SessionExpired";
import securityAlert from '../assets/exclamationSecurity.png';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

export const changeTokenValidity = () => {
  return {
    type: 'token-has-expired',
    payload: false
  };
};

const PostFeed = (props) => {
  const dispatch = useDispatch();
  const [page, setPage] = useState({ content: [] });
  const [isLoadingPosts, setLoadingPosts] = useState(true);
  const [hasLoadedPosts, setHasLoadedPosts] = useState(false);
  const [isClosedModal, setClosedModal] = useState();
  const [isLoadingOldPosts, setLoadingOldPosts] = useState(false);
  const [isLoadingNewPosts, setLoadingNewPosts] = useState(false);
  const [isDeletingPost, setDeletingPost] = useState(false);
  const [newPostCount, setNewPostsCount] = useState(0);
  const [postToBeDeleted, setPostToBeDeleted] = useState();

  const [hasFullAccess, setHasFullAccess] = useState(props.loggedInUser.isLoggedIn ? true : false);
  const intervalRef = useRef(null);

  useEffect(() => {
    const loadPosts = () => {
      if (hasFullAccess) {
        setLoadingPosts(true);
        apiCalls
          .loadPosts(props.user, props.loggedInUser.jwt)
          .then((response) => {
            setLoadingPosts(false);
            setHasLoadedPosts(true);
            setPage(response.data);
          })
          .catch((error) => {
            dispatch(changeTokenValidity());
            setHasFullAccess(false);
            setLoadingPosts(false);
          });
      } else {
        setLoadingPosts(false);
      }
    };
    loadPosts();
  }, [props.user, dispatch, props.loggedInUser, hasFullAccess]);

  useEffect(() => {
    const checkCount = () => {
      const posts = page.content;
      let toppostId = 0;
      if (posts.length > 0) {
        toppostId = posts[0].id;
      }
      if (hasFullAccess && hasLoadedPosts) {
        apiCalls
          .loadNewPostsCount(toppostId, props.user, props.loggedInUser.jwt)
          .then((response) => {
            setNewPostsCount(response.data.count);
          })
          .catch((error) => {
            if (props.user) {
              props.fromChildToParentCallback(true);
            }
            dispatch(changeTokenValidity());
            setHasFullAccess(false);
          })
      }
    };

    if (hasFullAccess) {
      intervalRef.current = setInterval(checkCount, 1500);

      return function cleanup() {
        if (isLoadingNewPosts) {
          clearInterval(intervalRef.current);
          intervalRef.current = setInterval(checkCount, 50);
        }
        clearInterval(intervalRef.current);
      };
    }

  }, [props.user, props, dispatch, page.content, isLoadingNewPosts, props.loggedInUser, hasFullAccess, hasLoadedPosts]);

  const onClickLoadMore = () => {
    if (isLoadingOldPosts) {
      return;
    }
    const posts = page.content;
    if (posts.length === 0) {
      return;
    }
    const postAtBottom = posts[posts.length - 1];
    setLoadingOldPosts(true);
    apiCalls
      .loadOldPosts(postAtBottom.id, props.user, props.loggedInUser.jwt)
      .then((response) => {
        setPage((previousPage) => ({
          ...previousPage,
          last: response.data.last,
          content: [...previousPage.content, ...response.data.content],
        }));
        setLoadingOldPosts(false);
      })
      .catch((error) => {
        setLoadingOldPosts(false);
      });
  };

  const onClickLoadNew = () => {
    if (isLoadingNewPosts) {
      return;
    }
    const posts = page.content;
    let toppostId = 0;
    if (posts.length > 0) {
      toppostId = posts[0].id;
    }
    setLoadingNewPosts(true);
    clearInterval(intervalRef.current);
    intervalRef.current = null;
    apiCalls
      .loadNewPosts(toppostId, props.user, props.loggedInUser.jwt)
      .then((response) => {
        setPage((previousPage) => ({
          ...previousPage,
          content: [...response.data, ...previousPage.content],
        }));
        setLoadingNewPosts(false);
        setNewPostsCount(0);
      })
      .catch((error) => {
        setLoadingNewPosts(false);
      });
  };

  const onClickModalOk = () => {
    setDeletingPost(true);
    apiCalls.deletePost(postToBeDeleted.id, props.loggedInUser.jwt).then((response) => {
      setPage((previousPage) => ({
        ...previousPage,
        content: previousPage.content.filter(
          (post) => post.id !== postToBeDeleted.id
        ),
      }));
      setDeletingPost(false);
      setPostToBeDeleted();
      setClosedModal(false);
    });
  };

  const onClickModalCancel = () => {
    setPostToBeDeleted();
    setClosedModal(false);
  };

  const onReact = (currentPost, reaction) => {
    apiCalls.postReaction(currentPost.id, reaction, props.loggedInUser.jwt).then(res => {
      const posts = page.content.filter(post => {
        if (post.id !== currentPost.id) {
          return post;
        }

        const previousReaction = post.reactions.loggedUserReaction;

        if (previousReaction === 'LIKE') {
          post.reactions.likeCount -= 1;
        } else if (previousReaction === 'DISLIKE') {
          post.reactions.dislikeCount -= 1;
        }

        const newReaction = reaction.toUpperCase();
        if (previousReaction === newReaction) {
          post.reactions.loggedUserReaction = null;
        } else {
          post.reactions.loggedUserReaction = newReaction;
          if (newReaction === 'LIKE') {
            post.reactions.likeCount += 1;
          } else {
            post.reactions.dislikeCount += 1;
          }
        }
        return post;
      })
      setPage((previousPage) => ({
        ...previousPage,
        content: posts
      }));
    });
  }

  if (isLoadingPosts) {
    return <Spinner />;
  }

  if (!hasFullAccess && !props.loggedInUser.isLoggedIn) {
    return <div className="pt-4 mt-2">
      <AuthNeeded />
    </div>;
  }

  if (page.content.length === 0 && newPostCount === 0 && hasFullAccess) {
    return (
      <div className="container">
        {(props.user == null || props.loggedInUser.username === props.user) ?
          (<div className="pt-4">
            <Row>
              <Col xs={12} md={12} lg={12} xl={12}>
                <PostSubmit />
              </Col>
            </Row>
          </div>
          ) : (<div className="pb-4 mb-3" />)}
        <div className="pb-5 mb-5">
          <div className="card card-header text-center">There are no posts</div>
        </div>
      </div>
    );
  }
  const newPostCountMessage =
    newPostCount === 1
      ? "There is 1 new post"
      : `There are ${newPostCount} new posts`;

  return (
    <div>
      {hasFullAccess ? (<div className="pt-4">
        <div>
          {(props.user == null || props.loggedInUser.username === props.user) &&
            (<div className="container">
              <PostSubmit />
            </div>
            )}
        </div>
        {(newPostCount > 0 && props.loggedInUser.isLoggedIn) && (
          <div className="container pb-1">
            <div
              className="card card-header text-center"
              onClick={onClickLoadNew}
              style={{
                cursor: isLoadingNewPosts ? "not-allowed" : "pointer",
              }}
            >
              {isLoadingNewPosts ? <Spinner /> : newPostCountMessage}
            </div>
            {page.content.length === 0 && (<div className="pb-5 mb-5" />)}
          </div>
        )}
        {page.content.map((post) => {
          return (
            <div className="container pb-1">
              <PostView
                key={post.id}
                post={post}
                onClickDelete={() => setPostToBeDeleted(post)}
                onClickLike={() => onReact(post, 'like')}
                onClickDislike={() => onReact(post, 'dislike')}
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
                cursor: isLoadingOldPosts ? "not-allowed" : "pointer",
              }}
            >
              {isLoadingOldPosts ? <Spinner /> : "View More Posts"}
            </div>
          </div>
        )}
        <ModalView
          visible={postToBeDeleted && true}
          isClosed={isClosedModal}
          onClickCancel={onClickModalCancel}
          body={
            postToBeDeleted &&
            "Are you sure you want to remove this post? This cannot be undone."
          }
          title="Delete post"
          okButton="Delete Post"
          onClickOk={onClickModalOk}
          pendingApiCall={isDeletingPost}
        />
      </div>) : (<div className="pt-4">
        {(!hasFullAccess && props.loggedInUser.isLoggedIn) &&
          (<div className="mt-1">
            <div className="card mb-3 verticalLineSecurity">
              <Row>
                <Col xs={12} md={12} lg={12} xl={11}>
                  <div className="card-body d-flex flex-column ">
                    <p className="text-secondary mb-0">
                      You need to be authenticated to access this resource!
                    </p>
                  </div>
                </Col>

                <Col xs={1} md={1} lg={1} xl={1}>
                  <div className="d-flex justify-content-center securityPostSubmit">
                    <img className="m-auto" src={securityAlert} width="26" alt="SecurityAlert" />
                  </div>
                </Col>

              </Row>
            </div>

            <div className="pt-2">
              <SessionExpired />
            </div>
          </div>
          )}
      </div>
      )}
    </div>
  );
};


const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(PostFeed);
