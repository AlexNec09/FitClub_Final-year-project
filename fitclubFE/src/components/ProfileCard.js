import React from 'react';
import ProfileImageWithDefault from './ProfileImageWithDefault';
import Input from './Input';
import ButtonWithProgress from './ButtonWithProgress';

const ProfileCard = (props) => {
    const { displayName, username, image, followed } = props.user;

    console.log(props.user);

    const showEditButton = props.isEditable && !props.inEditMode;

    return (
        <div className='card'>
            <div className='card-header text-center'>
                <ProfileImageWithDefault alt="profile"
                    width="200"
                    height="200"
                    image={image}
                    src={props.loadedImage}
                    className='shadow rounded-circle' />
            </div>
            <div className='card-body text-center'>
                {!props.inEditMode && <h4>{`${displayName}@${username}`}</h4>}
                {props.inEditMode && (
                    <div className="mb-2">
                        <Input
                            value={displayName}
                            label={`Change Display Name for ${username}`}
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
                {showEditButton && (<button className="btn btn-outline-success" onClick={props.onClickEdit}>
                    <i className="fas fa-user-edit" /> Edit
                </button>
                )}
                {
                    props.inEditMode && (
                        <div>
                            <ButtonWithProgress
                                className="btn btn-primary"
                                onClick={props.onClickSave}
                                disabled={props.pendingUpdateCall}
                                text={
                                    <span className='mx-2'>
                                        <i className="fas fa-save" /> Save
                                    </span>
                                }
                                pendingApiCall={props.pendingUpdateCall}

                            />
                            <button className="btn btn-outline-secondary mx-3"
                                onClick={props.onClickCancel}
                                disabled={props.pendingUpdateCall}
                            >
                                <i className="fas fa-window-close" /> Cancel
                            </button>
                        </div>
                    )}
                {props.isFollowable && <div className="mx-2 text-center">
                    <ButtonWithProgress
                        className="btn btn-primary"
                        onClick={props.onToggleFollow}
                        disabled={props.pendingFollowToggleCall}
                        pendingApiCall={props.pendingFollowToggleCall}
                        text={followed ? 'Unfollow' : 'Follow'}
                    />
                </div>}
            </div>
        </div>
    );
};

ProfileCard.defaultProps = {
    errors: {}
}

export default ProfileCard;