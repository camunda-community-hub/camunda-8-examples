import {useEffect, useState} from "react";

export default function MyForm({data, setResult}) {
  const [myText, setMyText] = useState(data.myText || "");
  const myTextChanged = (e) => setMyText(e.target.value);

  useEffect(() => {
    setResult({myText})
  }, [myText]);

  return (
      <input type="text" onChange={myTextChanged} value={myText}/>
  );
}