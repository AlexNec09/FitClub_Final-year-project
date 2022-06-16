import React, { useRef } from "react";
import logo from "../assets/mylogo.png";
import { Link } from "react-router-dom";
import { connect } from "react-redux";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import useClickTracker from "../shared/useClickTracker";

const TopBar = (props) => {
  const actionArea = useRef();
  const dropDownVisible = useClickTracker(actionArea);

  const onClickLogout = () => {
    const action = {
      type: "logout-success",
    };
    props.dispatch(action);
    window.location.reload();
  };

  let links = (
    <ul className="nav navbar-nav ms-auto mb-lg-0">
      <li className="nav-item">
        <Link to="/signup" className="nav-link">
          Sign Up
        </Link>
      </li>
      <li className="nav-item">
        <Link to="/login" className="nav-link">
          Login
        </Link>
      </li>
    </ul>
  );
  if (props.user.isLoggedIn) {
    let dropDownClass = "p-0 shadow dropdown-menu";
    if (dropDownVisible) {
      dropDownClass += " show";
    }
    links = (
      <ul className="navbar-nav ms-auto mb-lg-0" ref={actionArea}>
        <li className="nav-item dropdown">
          <div className="d-flex" style={{ cursor: "pointer" }}>
            <ProfileImageWithDefault
              className="rounded-circle m-auto"
              width="32"
              height="32"
              image={props.user.image}
            />
            <span className="nav-link dropdown-toggle">
              {props.user.displayName}
            </span>
          </div>
          <div className={dropDownClass} data-testid="drop-down-menu">
            <Link to={`/users/${props.user.username}`} className="dropdown-item">
              <i className="fas fa-user text-info mx-2"></i>My Profile
            </Link>
            <span
              className="dropdown-item"
              onClick={onClickLogout}
              style={{
                cursor: "pointer",
              }}
            >
              <i className="fas fa-sign-out-alt text-danger mx-2"></i>Logout
            </span>
          </div>
        </li>
      </ul>
    );
  }
  return (
    <div className="bg-white shadow-sm mb-2 sticky-header">
      <div className="container">
        <nav className="navbar navbar-light navbar-expand">
          <Link to="/" className="navbar-brand pt-1">
            <img src={logo} width="35" alt="FitClub" className="pb-1" /> FitClub
          </Link>
          {links}
        </nav>
      </div>
    </div>
  );
};

const mapStateToProps = (state) => {
  return {
    user: state,
  };
};

export default connect(mapStateToProps)(TopBar);
