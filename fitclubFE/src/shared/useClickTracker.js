import { useEffect, useState } from "react";

const useClickTracker = (actionArea) => {
  const [dropDownVisibile, setDropDownVisible] = useState(false);

  useEffect(() => {
    const onClickTracker = (event) => {
      if (!actionArea.current) {
        setDropDownVisible(false);
        return;
      }
      if (dropDownVisibile) {
        setDropDownVisible(false);
      } else if (actionArea.current.contains(event.target)) {
        setDropDownVisible(true);
      }
    };
    document.addEventListener("click", onClickTracker);

    return function cleanup() {
      document.removeEventListener("click", onClickTracker);
    };
  }, [actionArea, dropDownVisibile]);

  return dropDownVisibile;
};

export default useClickTracker;
