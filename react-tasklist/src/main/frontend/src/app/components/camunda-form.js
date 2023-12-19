'use client'

import {useEffect, useRef} from "react";
import {Form} from '@bpmn-io/form-js-viewer';

export default function CamundaFormContainer({schema, data, onComplete}) {
  const submit  = () => form.current.submit()
  const formContainer = useRef();
  const form = useRef();
  const containsSubmit =  schema && schema.components && schema.components.some(comp => comp.type === 'button' && comp.action === 'submit');

  useEffect( () => {
    form.current = new Form({
      container: formContainer.current
    });

    form.current.importSchema(schema, data);
    form.current.on('submit', (event) => {
      onComplete(event.data);
    });
  }, []);


  return (
      <>
        <link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:ital,wght@0,400;0,600;1,400&display=swap"
              rel="stylesheet"/>
        <link href="https://unpkg.com/@bpmn-io/form-js/dist/assets/form-js.css" rel="stylesheet"/>
        <div ref={formContainer}></div>
        {containsSubmit ? (<></>
        ) : (<button onClick={submit}>Submit</button>)
        }

      </>
  )
}