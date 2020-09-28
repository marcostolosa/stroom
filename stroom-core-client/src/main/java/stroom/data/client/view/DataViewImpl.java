/*
 * Copyright 2016 Crown Copyright
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

package stroom.data.client.view;

import stroom.data.client.presenter.DataPresenter.DataView;
import stroom.data.pager.client.SubStreamNavigator;
import stroom.svg.client.SvgPreset;
import stroom.util.shared.HasSubStreams;
import stroom.widget.button.client.ButtonPanel;
import stroom.widget.button.client.ButtonView;
import stroom.widget.button.client.ToggleButtonView;
import stroom.widget.tab.client.view.LinkTabBar;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.LayerContainer;
import com.gwtplatform.mvp.client.ViewImpl;

public class DataViewImpl extends ViewImpl implements DataView {
    private final Widget widget;
    @UiField
    LinkTabBar tabBar;

//    @UiField(provided = true)
//    SvgButton rawBtn;
//    @UiField(provided = true)
//    SvgButton formattedBtn;
    @UiField
    ButtonPanel buttonPanel;

    @UiField
    SubStreamNavigator subStreamNavigator;

//    @UiField
//    Pager segmentPager;
//    @UiField
//    Pager dataPager;
    @UiField
    LayerContainer layerContainer;

    @Inject
    public DataViewImpl(final Binder binder) {
        widget = binder.createAndBindUi(this);
        layerContainer.setFade(true);
//        segmentPager.setTitle("Segment");

//        rawBtn = SvgButton.create(SvgPresets.RAW.title("View Raw"));
//        formattedBtn = SvgButton.create(SvgPresets.FORMAT.title("View Formatted"));

        subStreamNavigator.setVisible(false);
//        dataNavigator.setDisplay(new HasCharacterData() {
//            @Override
//            public boolean isMultiPart() {
//                return true;
//            }
//
//            @Override
//            public Optional<Long> getPartNo() {
//                return Optional.of(0L);
//            }
//
//            @Override
//            public Optional<Long> getTotalParts() {
//                return Optional.of(10L);
//            }
//
//            @Override
//            public void setPartNo(final long partNo) {
//
//            }
//
//            @Override
//            public boolean isSegmented() {
//                return true;
//            }
//
//            @Override
//            public Optional<Long> getSegmentNo() {
//                return Optional.of(1L);
//            }
//
//            @Override
//            public Optional<Long> getTotalSegments() {
//                return Optional.of(20L);
//            }
//
//            @Override
//            public void setSegmentNo(final long partNo) {
//            }
//
//            @Override
//            public Optional<Long> getCharFrom() {
//                return Optional.of(1L);
//            }
//
//            @Override
//            public Optional<Long> getCharTo() {
//                return Optional.of(100L);
//            }
//
//            @Override
//            public Optional<Long> getTotalChars() {
//                return Optional.of(1000L);
//            }
//
//            @Override
//            public void advanceCharactersForward() {
//
//            }
//
//            @Override
//            public void advanceCharactersBackwards() {
//
//            }
//        });
        subStreamNavigator.refresh();
    }

    @Override
    public ButtonView addButton(final SvgPreset preset) {
        return buttonPanel.addButton(preset);
    }

    @Override
    public ToggleButtonView addToggleButton(final SvgPreset onPreset,
                                            final SvgPreset offPreset) {
        return buttonPanel.addToggleButton(onPreset, offPreset);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

//    @Override
//    public void showSegmentPager(final boolean show) {
//        segmentPager.setVisible(show);
//    }
//
//    @Override
//    public void showDataPager(final boolean show) {
//        dataPager.setVisible(show);
//    }
//
//    @Override
//    public void setSegmentPagerRows(final HasRows display) {
//        segmentPager.setDisplay(display);
//    }
//
//    @Override
//    public void setSegmentPagerToVisibleState(final boolean isVisible) {
//        segmentPager.setToVisibleState(isVisible);
//    }
//
//    @Override
//    public void setDataPagerRows(final HasRows display) {
//        dataPager.setDisplay(display);
//    }
//
//    @Override
//    public void setSegmentPagerTitle(final String title) {
//        segmentPager.setTitle(title);
//    }
//
//    @Override
//    public void setDataPagerTitle(final String title) {
//        dataPager.setTitle(title);
//    }
//
//    @Override
//    public void setDataPagerToVisibleState(final boolean isVisible) {
//        dataPager.setToVisibleState(isVisible);
//    }

    @Override
    public LinkTabBar getTabBar() {
        return tabBar;
    }

    @Override
    public LayerContainer getLayerContainer() {
        return layerContainer;
    }

    @Override
    public void setRefreshing(final boolean refreshing) {
        subStreamNavigator.setRefreshing(refreshing);
    }

    @Override
    public void setNavigatorData(final HasSubStreams dataNavigatorData) {
        subStreamNavigator.setDisplay(dataNavigatorData);
    }

    @Override
    public void refreshNavigator() {
        subStreamNavigator.refresh();
    }

    @Override
    public void setNavigatorClickHandler(final Runnable clickHandler) {
        subStreamNavigator.setClickHandler(clickHandler);
    }

    public interface Binder extends UiBinder<Widget, DataViewImpl> {
    }
}
