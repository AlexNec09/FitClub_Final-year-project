import React from "react";
import defaultPicture from "../assets/profile.png";
import { IMAGES_PROFILE } from "../config";

const ProfileImageWithDefault = (props) => {
  let imageSource = defaultPicture;
  if (props.image) {
    imageSource = `${IMAGES_PROFILE}/${props.image}`;
  }
  return (
    <img
      alt="profilePic"
      {...props}
      src={props.src || imageSource}
      onError={(event) => {
        event.target.src = defaultPicture;
      }}
    />
  );
};

export default ProfileImageWithDefault;
