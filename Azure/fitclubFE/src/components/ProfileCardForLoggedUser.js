import React from 'react';
import ProfileImageWithDefault from './ProfileImageWithDefault';
import ButtonWithProgress from './ButtonWithProgress';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import personalInfo from '../assets/personalInfo.png';
import personalInfo2 from '../assets/personalInfo2.png';
import confirmationEmail from '../assets/confirmationEmail.png';
import { connect } from "react-redux";
import Input from './Input';
import { format } from 'timeago.js';

const ProfileCardForLoggedUser = (props) => {
    const { username, displayName, image, emailVerificationStatus, date, followed, follows, followedBy } = props.user;

    const showEditButton = props.isEditable && !props.inEditMode;

    const relativeDate = format(date);


    return (
        <React.Fragment>
            <div className="card shadow-sm">
                <Row>
                    <Col xs={12} md={12} xl={4}>
                        <div className="pt-3 pb-3 pl-2 text-center">
                            <ProfileImageWithDefault
                                alt="profile"
                                width="205"
                                height="205"
                                image={image}
                                src={props.loadedImage}
                                className="rounded-circle shadow-sm"
                            />
                        </div>
                    </Col>
                    <Col xs={12} md={12} xl={5}>
                        <div className="card-body d-flex flex-column m-2 float-left pl-3 mt-2">
                            <Row className="pt-5 mb-0">
                                <p className="text-center text-login-page font-weight-bold notClickable-text">Followers:&nbsp;{followedBy}</p>
                            </Row>

                            <Row className="mb-0">
                                <p className="text-center text-login-page font-weight-bold notClickable-text">Following:&nbsp;{follows}</p>
                            </Row>

                            <Row>
                                <p className="text-center text-login-page text-secondary notClickable-text">
                                    Registration date:&nbsp;{relativeDate}
                                </p>
                            </Row>

                            <Row>
                                {props.isFollowable && <div className=" text-center mt-4">
                                    <ButtonWithProgress
                                        className="custom-row-follow-button"
                                        onClick={props.onToggleFollow}
                                        disabled={props.pendingFollowToggleCall}
                                        pendingApiCall={props.pendingFollowToggleCall}
                                        text={followed ? 'Unfollow' : 'Follow'}
                                    />
                                </div>}
                            </Row>

                        </div>
                    </Col>

                    <Col xs={12} md={12} lg={6} xl={3}>
                        <div className="d-flex justify-content-center personalInfo mt-1 pe-4 pb-3">
                            <img className="" src={personalInfo} width="250" alt="PersonalInfoImg" />
                        </div>

                    </Col>
                </Row>
            </div>

            <div className="card mt-4 p-2 shadow-sm">
                <Row>
                    <Col xs={12} md={12} lg={8}>
                        <div className="card-body d-flex flex-column m-2 float-left pl-3 mt-2">
                            {!props.inEditMode && (
                                // <h4>Displayname: {`${displayName}`} 
                                // <br></br>
                                // Email: {`${username}`} </h4>
                                <div className="float-left pl-5">

                                    <Row className="pt-1">
                                        <div className="text-center font-weight-bold">Display Name:</div>
                                        <p data-testid="displayNameTestId" className="text-center text-login-page text-secondary notClickable-text">
                                            &nbsp;{displayName}
                                        </p>
                                    </Row>
                                    <Row>
                                        <div className="text-center font-weight-bold">Username:</div>
                                        <p className="text-center text-login-page text-secondary notClickable-text">
                                            &nbsp;{username}
                                        </p>

                                    </Row>

                                    {props.loggedInUser.username === username && (<Row>
                                        <div className="text-center font-weight-bold">Email:</div>
                                        <p className="text-center text-login-page text-secondary notClickable-text">
                                            &nbsp;{props.user.email}
                                        </p>

                                    </Row>)}

                                </div>
                            )}

                            {props.inEditMode && (
                                <div className="">

                                    <Input
                                        value={displayName}
                                        // label={`Change Display Name for ${username}`}
                                        onChange={props.onChangeDisplayName}
                                        hasError={props.errors.displayName && true}
                                        error={props.errors.displayName}
                                    />


                                    <div className="mt-2">
                                        <Input
                                            type="file"
                                            onChange={props.onFileSelect}
                                            hasError={props.errors.image && true}
                                            error={props.errors.image}
                                        />
                                    </div>

                                </div>
                            )}

                            {props.inEditMode && (
                                <div className="row m-1 pt-1">
                                    <div className="d-flex justify-content-center">
                                        <ButtonWithProgress
                                            className="btn btn-primary"
                                            onClick={props.onClickSave}
                                            disabled={props.pendingUpdateCall}
                                            text={
                                                <span className='mx-2'>
                                                    <i className="fas fa-save mr-2" /> Save
                                                </span>
                                            }
                                            pendingApiCall={props.pendingUpdateCall}
                                        />
                                        <button className="btn btn-outline-secondary mx-3"
                                            onClick={props.onClickCancel}
                                            disabled={props.pendingUpdateCall}
                                        >
                                            <i className="fas fa-times mr-2 " /> Cancel
                                        </button>
                                    </div>
                                </div>
                            )}

                            <div className="container text-center mt-1 ml-auto">
                                {showEditButton && (
                                    <button
                                        className="custom-row-edit-button"
                                        onClick={props.onClickEdit}
                                    >
                                        <i className="far fa-edit mr-2"> </i>
                                        &nbsp;&nbsp;Edit Profile
                                    </button>
                                )}
                            </div>

                        </div>
                    </Col>

                    <Col xs={12} md={12} lg={4}>
                        <div className="d-flex justify-content-center personalInfo2 ps-5 p-4 pb-2">
                            <img className="m-auto" src={personalInfo2} width="300" alt="PersInfo" />
                        </div>
                    </Col>

                </Row>
            </div>

            {(props.loggedInUser.isLoggedIn && props.loggedInUser.username === username) && (<div className="card mt-4 p-2 shadow-sm">
                <Row>
                    <Col xs={12} md={12} lg={12} xl={8}>
                        <div className="card-body d-flex flex-column mt-2">
                            {/* {`${emailVerificationStatus}`} */}
                            {emailVerificationStatus && (

                                <h5 className="text-center text-success font-weight-bold pt-4 text-center success-text-resend">
                                    <span className="far fa-check-circle fa-lg fa-1.5x"></span>
                                    <span className="address-confirmed-text">&nbsp;Email address confirmed!</span>
                                </h5>
                            )}
                        </div>
                    </Col>

                    <Col xs={12} md={12} lg={12} xl={4}>
                        <div className="d-flex justify-content-center pr-5 pt-4 pb-2">
                            <img className="m-auto" src={confirmationEmail} width="230" alt="Hoaxify" />
                        </div>
                    </Col>

                </Row>
            </div>)}



        </React.Fragment>
    );
};

ProfileCardForLoggedUser.defaultProps = {
    errors: {}
}

const mapStateToProps = (state) => {
    return {
        loggedInUser: state,
    };
};

export default connect(mapStateToProps)(ProfileCardForLoggedUser);