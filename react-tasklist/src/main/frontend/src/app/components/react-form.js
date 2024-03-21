import { useState } from "react";
import myForm from './forms/some-form';

const forms = {
  myForm
}

export default function ReactFormContainer({formKey,data,onComplete}) {
  const [result,setResult] = useState({})
  const sanitizedFormKey = formKey.substring("react:".length)
  const ReactForm = forms[sanitizedFormKey]
  const submit = () => onComplete(result)
  return (
      <>
        { ReactForm ?
            (<>
          <ReactForm data={data} setResult={setResult}></ReactForm>
          <button onClick={submit}>Submit</button>
            </>) : (<p>Form could not be found...</p>)
        }

      </>
  )
}