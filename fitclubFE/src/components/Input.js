import React from "react";

const Input = (props) => {
  let inputClassName = "custom-row-input";

  if (props.type === "file") {
    inputClassName += "-file";
  }
  if (props.hasError !== undefined) {
    inputClassName += props.hasError ? " is-invalid" : " is-valid";
  }

  return (
    <div>
      {props.label && <label className="mt-2 custom-row-label">{props.label}</label>}
      <input
        name={props.name}
        className={inputClassName}
        type={props.type || "text"}
        placeholder={props.placeholder}
        value={props.value}
        onChange={props.onChange}
      />
      {props.hasError && (
        <span className="invalid-feedback-forprofileCard">{props.error}</span>
      )}
    </div>
  );
};

// const Input = (props) => {
//   let inputClassName = 'form-control-user-input form-group p-2 input';

//   if (props.hasError !== undefined) {
//     inputClassName += props.hasError ? ' is-invalid' : ' is-valid';
//   }

//   return (
//     <div >
//       <input
//         className={inputClassName}
//         type={props.type || 'text'}
//         placeholder={props.placeholder}
//         value={props.value}
//         onChange={props.onChange}
//       />
//       <label className="label">
//         {props.label}
//       </label>
//       {props.hasError && (
//         <span className="invalid-feedback invalid-input">{props.error}</span>
//       )}
//     </div>
//   );
// };

Input.defaultProps = {
  onChange: () => { },
};

export default Input;
