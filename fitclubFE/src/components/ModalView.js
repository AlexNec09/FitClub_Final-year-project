import React, { Component } from "react";
import ButtonWithProgress from "./ButtonWithProgress";

import Modal from "react-bootstrap/Modal";
import ModalHeader from "react-bootstrap/ModalHeader";
import ModalBody from "react-bootstrap/ModalBody";
import ModalFooter from "react-bootstrap/ModalFooter";
import { ModalTitle } from "react-bootstrap";

class ModalView extends Component {
  state = {
    demoModal: false,
  };

  componentDidUpdate(prevProps) {
    if (this.props.body !== undefined && prevProps.body !== this.props.body) {
      this.setState({ demoModal: true });
    }

    if (
      this.props.isClosed === false &&
      this.props.body === undefined &&
      prevProps.body !== undefined
    ) {
      this.setState({ demoModal: false });
    } else if (
      prevProps.pendingApiCall === true &&
      prevProps.body !== this.props.body
    ) {
      this.setState({ demoModal: false });
    }
  }

  onClickCancel = () => {
    this.setState({ demoModal: false });
  };

  render() {
    const {
      title,
      visible,
      onClickCancel,
      body,
      okButton,
      cancelButton,
      onClickOk,
      pendingApiCall,
    } = this.props;

    let rootStyle;
    if (visible) {
      rootStyle = { backgroundColor: "#000000b0", display: "block" };
      return (
        <div>
          <Modal
            scrollable={true}
            show={this.state.demoModal}
            fade={false}
            style={rootStyle}
            dialogClassName={"primaryModal"}
          >
            <ModalHeader toggle={this.toggle} as="span">
              <ModalTitle as="h5">{title}</ModalTitle>
            </ModalHeader>
            <ModalBody as="span" style={{ flexWrap: "wrap" }}>
              {body}
            </ModalBody>
            <ModalFooter>
              <button
                className="btn btn-secondary"
                onClick={onClickCancel}
                disabled={pendingApiCall}
              >
                {cancelButton}
              </button>
              <ButtonWithProgress
                className="btn btn-danger"
                onClick={onClickOk}
                disabled={pendingApiCall}
                pendingApiCall={pendingApiCall}
                text={okButton}
              />
            </ModalFooter>
          </Modal>
        </div>
      );
    } else {
      return <div></div>;
    }
  }
}

ModalView.defaultProps = {
  okButton: "Delete Post",
  cancelButton: "Cancel",
};

export default ModalView;
