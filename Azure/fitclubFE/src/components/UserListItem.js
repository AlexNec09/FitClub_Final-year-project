import React from "react";
import ProfileImageWithDefault from "./ProfileImageWithDefault";
import { Link } from "react-router-dom";

const UserListItem = (props) => {
    return (

        <Link to={`/users/${props.user.username}`} className="list-group-item list-group-item-action">
            <div className="d-flex">
                <ProfileImageWithDefault
                    className="rounded-circle m-1"
                    width="48"
                    height="48"
                    image={props.user.image}
                />
                <div className="flex-fill m-auto ps-2">
                    <div>
                        <h6 className="d-inline display-name">
                            {props.user.displayName}
                        </h6>

                        <br />
                        <span className="text-black-50 fs-6">{props.user.username}</span>
                    </div>
                </div>
            </div>

        </Link>




    );
};

export default UserListItem;