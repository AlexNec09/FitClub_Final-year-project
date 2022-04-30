import React from "react";

const ButtonWithProgress = (props) => {
  return (
    <button
      className={props.className || "btn btn-primary"}
      onClick={props.onClick}
      disabled={props.disabled}
    >
      {props.pendingApiCall && (
        <span
          className="spinner-grow spinner-grow-sm mx-1"
          role="status"
        ></span>
      )}
      {props.text}
    </button>
  );
};

export default ButtonWithProgress;
