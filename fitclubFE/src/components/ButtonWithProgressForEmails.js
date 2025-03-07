import React from 'react';
import Spinner from './Spinner';

const ButtonWithProgressForEmails = (props) => {
    return (
        <button
            className="btn email-button p-2"
            onClick={props.onClick}
            disabled={props.disabled}
        >
            {props.values}
            <span className="text-button-confirmation">{props.value}</span>

            {props.pendingApiCall && (
                <Spinner />
            )}

            { /*           <div className="spinner-border spinner-border-sm mr-1">
                    <span className="sr-only">Loading...</span>
            </div> */}

            {!props.pendingApiCall && (
                <i className="fas fa-arrow-right pb-1 arrowIcon-button"></i>
            )}

        </button>
    )
}

export default ButtonWithProgressForEmails;