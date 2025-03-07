import React, { Component } from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import { connect } from "react-redux";
import * as apiCalls from "../api/apiCalls";
import ButtonWithProgress from "./ButtonWithProgress";
import Input from "./Input";
import securityAlert from '../assets/exclamationSecurity.png';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

class PostSubmit extends Component {
  state = {
    focused: false,
    content: undefined,
    pendingApiCall: false,
    errors: {},
    fileError: undefined,
    file: undefined,
    image: undefined,
    attachment: undefined,
  };

  onChangeContent = (event) => {
    const value = event.target.value;
    this.setState({
      content: value,
      errors: {},
    });
  };

  onFileSelect = (event) => {
    this.setState({ pendingApiCall: true, });
    if (event.target.files.length === 0) {
      return;
    }

    const file = event.target.files[0];
    let reader = new FileReader();
    reader.onloadend = () => {
      this.setState(
        {
          image: reader.result,
          errors: {},
          file,
        },
        () => {
          this.uploadFile();
        }
      );
    };
    reader.readAsDataURL(file);
  };

  uploadFile = () => {
    const body = new FormData();
    body.append("file", this.state.file);
    apiCalls.postUserPostFile(body, this.props.loggedInUser.jwt).then((response) => {
      this.setState({ fileError: undefined, attachment: response.data, pendingApiCall: false });
    }).catch((error) => {
      let fileError;
      if (error.response && error.response.data) {
        fileError = error.response.data.message;
      } else {
        fileError = "The uploaded file exceeds maximum permitted size of 10MB."
      }
      this.setState({ fileError, pendingApiCall: false });
    });
  };

  resetState = () => {
    this.setState({
      pendingApiCall: false,
      focused: false,
      content: "",
      errors: {},
      fileError: undefined,
      image: undefined,
      file: undefined,
      attachment: undefined,
    });
  };

  onClickSend = () => {
    const body = {
      content: this.state.content,
      attachment: this.state.attachment,
    };

    this.setState({
      pendingApiCall: true,
    });

    apiCalls
      .postUserPost(body, this.props.loggedInUser.jwt)
      .then((response) => {
        this.resetState();
      })
      .catch((error) => {
        let errors = {};
        if (error.response.data && error.response.data.validationErrors) {
          errors = error.response.data.validationErrors;
        }
        this.setState({
          pendingApiCall: false,
          errors,
        });
      });
  };

  onFocus = () => {
    this.setState({
      focused: true,
    });
  };

  render() {
    let textAreaClassName = "form-control w-100";
    let inputClassName = "pt-2";

    if (this.state.errors.content) {
      textAreaClassName += " is-invalid";
    } else if (this.state.fileError) {
      inputClassName += " is-invalid";
    }
    return (
      <div className="pb-3">
        {this.props.loggedInUser.isLoggedIn ? (<div className="card d-flex flex-row p-2">
          <ProfileImageWithDefault
            className="rounded-circle m-1"
            width="32"
            height="32"
            image={this.props.loggedInUser.image}
          />
          <div className="flex-fill">
            <textarea
              className={textAreaClassName}
              rows={this.state.focused ? 3 : 1}
              placeholder="Share something with your followers"
              onFocus={this.onFocus}
              value={this.state.content}
              onChange={this.onChangeContent}
            />

            {this.state.errors.content && (
              <span className="invalid-feedback">
                {this.state.errors.content}
              </span>
            )}


            {this.state.focused && (
              <div>
                <div className={inputClassName}>
                  <Input type="file" accept="image/png, image/jpeg, image/gif" onChange={this.onFileSelect} />
                  {this.state.image && (
                    <img
                      className="mt-2 img-thumbnail"
                      src={this.state.image}
                      alt="Uploaded File"
                      width="128"
                      height="64"
                    />
                  )}
                </div>

                {this.state.fileError && (
                  <span className="invalid-feedback pt-2">
                    {this.state.fileError}
                  </span>
                )}


                <div className="text-end mt-2">
                  <ButtonWithProgress
                    className="btn btn-success"
                    disabled={this.state.pendingApiCall || this.state.fileError}
                    onClick={this.onClickSend}
                    pendingApiCall={this.state.pendingApiCall}
                    text="Send"
                  />
                  <button
                    className="btn btn-light ms-1"
                    onClick={this.resetState}
                    disabled={this.state.pendingApiCall}
                  >
                    <i className="fas fa-times"></i> Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>) : (<div className="card mb-3 verticalLineSecurity">
          <Row>
            <Col xs={11} md={11} lg={11} xl={11}>
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
        </div>)}
      </div>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state,
  };
};

export default connect(mapStateToProps)(PostSubmit);
