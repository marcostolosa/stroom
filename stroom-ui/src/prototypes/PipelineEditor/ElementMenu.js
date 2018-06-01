/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';

import { compose } from 'redux';
import { connect } from 'react-redux';

import { Dropdown } from 'semantic-ui-react';

import {
  initiateAddPipelineElement,
  requestDeletePipelineElement,
  closePipelineElementContextMenu,
} from './redux';

const ElementMenu = ({
  pipelineId,
  elementId,
  isOpen,
  closePipelineElementContextMenu,
  initiateAddPipelineElement,
  requestDeletePipelineElement,
}) => (
  <Dropdown
    floating
    direction="right"
    icon={null}
    open={isOpen}
    onClose={() => closePipelineElementContextMenu(pipelineId)}
  >
    <Dropdown.Menu>
      <Dropdown.Item
        icon="add"
        text="Add"
        onClick={() => initiateAddPipelineElement(pipelineId, elementId)}
      />
      <Dropdown.Item
        icon="trash"
        text="Delete"
        onClick={() => requestDeletePipelineElement(pipelineId, elementId)}
      />
    </Dropdown.Menu>
  </Dropdown>
);

ElementMenu.propTypes = {
  pipelineId: PropTypes.string.isRequired,
  elementId: PropTypes.string.isRequired,
  isOpen: PropTypes.bool.isRequired,

  requestDeletePipelineElement: PropTypes.func.isRequired,
  closePipelineElementContextMenu: PropTypes.func.isRequired,
  initiateAddPipelineElement: PropTypes.func.isRequired,
};

export default compose(connect(
  state => ({
    // state
  }),
  {
    closePipelineElementContextMenu,
    initiateAddPipelineElement,
    requestDeletePipelineElement,
  },
))(ElementMenu);
