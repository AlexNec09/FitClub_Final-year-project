import React, { useEffect, useReducer, useState } from "react";
import * as apiCalls from "../api/apiCalls";
import ProfileCard from "../components/ProfileCard";
import { connect } from "react-redux";
import PostFeed from "../components/PostFeed";
import Spinner from "../components/Spinner";
import Tab from 'react-bootstrap/Tab';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Nav from 'react-bootstrap/Nav';
import Security from '../components/Security';
import ProfileCardForLoggedUser from "../components/ProfileCardForLoggedUser";
import FeedPage from '../assets/FeedPage.png';
import userProfileIcon from '../assets/userProfile.jpg';
import securityIcon from '../assets/security.png';
import SessionExpired from "../components/SessionExpired";

const reducer = (state, action) => {
  if (action.type === "loading-user") {
    return {
      ...state,
      isLoadingUser: true,
      userNotFound: false,
    };
  } else if (action.type === "load-user-success") {
    return {
      ...state,
      isLoadingUser: false,
      user: action.payload,
    };
  } else if (action.type === "load-user-failure") {
    return {
      ...state,
      isLoadingUser: false,
      userNotFound: true,
    };
  } else if (action.type === "cancel") {
    let displayName = state.user.displayName;
    if (state.originalDisplayName) {
      displayName = state.originalDisplayName;
    }
    return {
      ...state,
      inEditMode: false,
      image: undefined,
      errors: {},
      originalDisplayName: undefined,
      user: {
        ...state.user,
        displayName,
      },
    };
  } else if (action.type === "update-in-progress") {
    return {
      ...state,
      pendingUpdateCall: true,
    };
  } else if (action.type === "update-success") {
    return {
      ...state,
      inEditMode: false,
      image: undefined,
      originalDisplayName: undefined,
      pendingUpdateCall: false,
      user: {
        ...state.user,
        image: action.payload,
      },
    };
  } else if (action.type === "update-failure") {
    return {
      ...state,
      pendingUpdateCall: false,
      errors: action.payload,
    };
  } else if (action.type === "update-displayName") {
    let originalDisplayName = state.originalDisplayName;
    if (!originalDisplayName) {
      originalDisplayName = state.user.displayName;
    }
    const errors = state.errors;
    errors.displayName = undefined;
    return {
      ...state,
      errors,
      originalDisplayName,
      user: {
        ...state.user,
        displayName: action.payload,
      },
    };
  } else if (action.type === "select-file") {
    const errors = state.errors;
    errors.image = undefined;
    return {
      ...state,
      errors,
      image: action.payload,
    };
  } else if (action.type === "edit-mode") {
    return {
      ...state,
      inEditMode: true,
    };
  } else if (action.type === "follow-op-init") {
    return {
      ...state,
      pendingFollowToggleCall: true,
    }
  } else if (action.type === "user-follow") {
    return {
      ...state,
      pendingFollowToggleCall: false,
      user: {
        ...state.user,
        followed: !action.payload.followed,
        followedBy: !action.payload.followed ? action.payload.followedBy + 1 : action.payload.followedBy - 1
      }
    }
  } else if (action.type === "follow-op-failure") {
    return {
      ...state,
      pendingFollowToggleCall: false,
    }
  }
  return state;
};

const UserPage = (props) => {
  const [state, dispatch] = useReducer(reducer, {
    user: undefined,
    userNotFound: false,
    isLoadingUser: false,
    inEditMode: false,
    originalDisplayName: undefined,
    pendingUpdateCall: false,
    pendingFollowToggleCall: false,
    image: undefined,
    errors: {},
  });

  const [sessionExpired, setSessionExpired] = useState(false);
  const token = props.loggedInUser.jwt;


  useEffect(() => {
    const loadUser = () => {
      const username = props.match.params.username;
      if (!username) {
        return;
      }
      dispatch({ type: "loading-user" });

      apiCalls.checkValidToken(token)
        .then((response) => {
          console.log(response.data);
          if (response.data.result === "VALID") {
            setSessionExpired(false);
          } else {
            setSessionExpired(true);
          }
        })
        .catch((e) => {
          setSessionExpired(true);
        });

      apiCalls
        .getUser(username)
        .then((response) => {
          dispatch({ type: "load-user-success", payload: response.data });
        })
        .catch((error) => {
          dispatch({ type: "load-user-failure" });
        });
    };
    loadUser();
  }, [props.match.params.username, token]);

  const onClickSave = () => {
    const userId = props.loggedInUser.id;
    const userUpdate = {
      displayName: state.user.displayName,
      image: state.image && state.image.split(",")[1],
    };
    dispatch({ type: "update-in-progress" });
    apiCalls
      .updateUser(userId, userUpdate, props.loggedInUser.jwt)
      .then((response) => {
        dispatch({ type: "update-success", payload: response.data.image });
        const updatedUser = { ...state.user };
        updatedUser.image = response.data.image;

        const action = {
          type: "update-success",
          payload: updatedUser,
        };
        props.dispatch(action); // dispatching this new data to redux
      })
      .catch((error) => {
        let errors = {};
        if (error.response.data.validationErrors) {
          errors = error.response.data.validationErrors;
        }
        dispatch({ type: "update-failure", payload: errors });
      });
  };

  const onFileSelect = (event) => {
    if (event.target.files.length === 0) {
      return;
    }
    const file = event.target.files[0];
    let reader = new FileReader();
    reader.onloadend = () => {
      dispatch({ type: "select-file", payload: reader.result });
    };
    reader.readAsDataURL(file);
  };

  const entering = (e) => {
    e.children[1].style.backgroundColor = 'rgba(0,0,0,0.4)';
  };

  const onToggleFollow = () => {
    dispatch({ type: "follow-op-init" });
    apiCalls.follow(state.user.id, !state.user.followed)
      .then((response) => {
        dispatch({ type: "user-follow", payload: state.user });
        const updatedUser = { ...state.user };
        updatedUser.followed = !state.user.followed;

        const action = {
          type: "user-follow",
          payload: updatedUser,
        };
        props.dispatch(action);
      }).catch(err => {
        dispatch({ type: "follow-op-failure" });
      });
  }

  const handleCallback = (isSessionExpired) => {
    setSessionExpired(isSessionExpired);
  }

  let pageContent;
  if (state.isLoadingUser) {
    pageContent = (
      <div className="container pt-4">
        <Spinner />
      </div>
    );
  } else if (state.userNotFound) {
    pageContent = (
      <div className="container pt-4">
        <div className="row">
          <div className="col">
            <div className="alert alert-danger text-center">
              <div className="alert-heading">
                <i className="fas fa-exclamation-triangle fa-3x" />
              </div>
              <br></br>
              <h5>User not found</h5>
            </div>
          </div>
          <div className="col">
            <div className="card card-header text-center">
              There are no posts
            </div>
          </div>
        </div>
      </div>
    );
  } else {
    const isEditable =
      props.loggedInUser.username === props.match.params.username;
    pageContent = state.user && (



      <div className="container pt-2">

        <Tab.Container
          id="left-tabs-example"
          defaultActiveKey="first">
          <Row className="pt-2 ">
            <Col className="" md={3} >

              <Nav variant="pills" className="flex-column sticky-menu">

                <Nav.Item >
                  <Nav.Link eventKey="first" className="my-nav-item">
                    {/* <i className="fas fa-user text-secondary pr-2"></i> */}
                    <img src={userProfileIcon} width="40" alt="UserProfile" />
                    <span className="pl-2"> Profile</span>

                  </Nav.Link>
                </Nav.Item>

                <Nav.Item>
                  {(props.loggedInUser.isLoggedIn && props.loggedInUser.username === state.user.username) && (<div>
                    <Nav.Link eventKey="second" className="my-nav-item">
                      {/* <i className="fas fa-unlock-alt text-secondary pr-2"></i> */}
                      <img src={securityIcon} width="40" alt="Security" />
                      <span className="pl-2"> Security</span>
                    </Nav.Link>
                  </div>)}
                </Nav.Item>

                <Nav.Item>
                  <Nav.Link eventKey="third" className="my-nav-item">
                    {/* <i className="fas fa-history text-secondary pr-2"></i> */}
                    <img src={FeedPage} width="40" alt="FeedPage" />
                    <span className="pl-2"> Feeds</span>
                  </Nav.Link>
                </Nav.Item>
              </Nav>

            </Col>

            <Col md={9}>
              <Tab.Content className="" >

                <Tab.Pane eventKey="first">

                  {!sessionExpired && props.loggedInUser.isLoggedIn ? (<div className="pt-4">
                    <ProfileCardForLoggedUser
                      user={state.user}
                      isEditable={isEditable}
                      inEditMode={state.inEditMode}
                      onClickEdit={() => dispatch({ type: "edit-mode" })}
                      onClickCancel={() => dispatch({ type: "cancel" })}
                      onClickSave={onClickSave}
                      onChangeDisplayName={(event) =>
                        dispatch({
                          type: "update-displayName",
                          payload: event.target.value,
                        })
                      }
                      pendingUpdateCall={state.pendingUpdateCall}
                      pendingFollowToggleCall={state.pendingFollowToggleCall}
                      entering={entering}

                      loadedImage={state.image}
                      onFileSelect={onFileSelect}
                      onToggleFollow={onToggleFollow}
                      isFollowable={props.loggedInUser.isLoggedIn && !isEditable}
                      errors={state.errors}

                      emailVerificationStatus={props.loggedInUser.emailVerificationStatus}
                    />

                  </div>) : (<div className="pt-4 mt-2">
                    <ProfileCard
                      user={state.user}
                      inEditMode={state.inEditMode}
                      onClickEdit={() => dispatch({ type: "edit-mode" })}
                      onClickCancel={() => dispatch({ type: "cancel" })} // inline function
                      onClickSave={onClickSave}
                      onChangeDisplayName={(event) =>
                        dispatch({
                          type: "update-displayName",
                          payload: event.target.value,
                        })
                      }
                      pendingUpdateCall={state.pendingUpdateCall}
                      loadedImage={state.image}
                      onFileSelect={onFileSelect}
                      isFollowable={props.loggedInUser.isLoggedIn && !isEditable}
                      onToggleFollow={onToggleFollow}
                      pendingFollowToggleCall={state.pendingFollowToggleCall}
                      errors={state.errors}
                    />
                    <div className="pt-4">
                      {props.loggedInUser.isLoggedIn &&
                        (
                          <div>
                            <SessionExpired />
                          </div>
                        )}
                    </div>
                  </div>


                  )}
                </Tab.Pane>

                <Tab.Pane eventKey="second">
                  <Security
                    user={state.user} isSessionExpired={sessionExpired}
                    emailVerificationStatus={props.loggedInUser.emailVerificationStatus}
                  />
                </Tab.Pane>

                <Tab.Pane eventKey="third">
                  <div className="row">

                    <div className="col">
                      <PostFeed user={props.match.params.username} fromChildToParentCallback={handleCallback} />

                    </div>

                  </div>
                </Tab.Pane>
              </Tab.Content>

            </Col>
          </Row>
        </Tab.Container>
      </div>
    );
  }
  return (
    <div className="background-image pt-5" id="background-image">

      <div data-testid="userpage">
        <div className="pb-4">
          <div className="col">{pageContent}</div>
        </div>
      </div>
    </div>
  );
};

UserPage.defaultProps = {
  match: {
    params: {},
  },
};

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(UserPage);
