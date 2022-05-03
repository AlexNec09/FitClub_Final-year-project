import React, { useEffect, useReducer } from "react";
import * as apiCalls from "../api/apiCalls";
import ProfileCard from "../components/ProfileCard";
import { connect } from "react-redux";
import MessageFeed from "../components/MessageFeed";
import Spinner from "../components/Spinner";

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
        followed: action.payload,
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

  useEffect(() => {
    const loadUser = () => {
      const username = props.match.params.username;
      if (!username) {
        return;
      }
      dispatch({ type: "loading-user" });
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
  }, [props.match.params.username]);

  const onClickSave = () => {
    const userId = props.loggedInUser.id;
    const userUpdate = {
      displayName: state.user.displayName,
      image: state.image && state.image.split(",")[1],
    };
    dispatch({ type: "update-in-progress" });
    apiCalls
      .updateUser(userId, userUpdate)
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

  const onToggleFollow = () => {
    dispatch({ type: "follow-op-init" });
    apiCalls.follow(state.user.id, !state.user.followed)
      .then((response) => {
        dispatch({ type: "user-follow", payload: !state.user.followed });
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

  let pageContent;
  if (state.isLoadingUser) {
    pageContent = <Spinner />;
  } else if (state.userNotFound) {
    pageContent = (
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
            There are no messages
          </div>
        </div>
      </div>
    );
  } else {
    const isEditable =
      props.loggedInUser.username === props.match.params.username;
    pageContent = state.user && (
      <div className="row">
        <div className="col">
          <ProfileCard
            user={state.user}
            isEditable={isEditable}
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
        </div>
        <div className="col">
          <MessageFeed user={props.match.params.username} />
        </div>
      </div>
    );
  }
  return (
    <div data-testid="userpage">
      <div className="row">
        <div className="col">{pageContent}</div>
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
