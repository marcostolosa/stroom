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

package stroom.feed.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import stroom.entity.client.presenter.ReadOnlyChangeHandler;
import stroom.feed.client.presenter.FeedSettingsPresenter.FeedSettingsView;
import stroom.feed.shared.Feed;
import stroom.feed.shared.Feed.FeedStatus;
import stroom.item.client.ItemListBox;
import stroom.item.client.StringListBox;
import stroom.pipeline.shared.SupportedRetentionAge;
import stroom.streamstore.shared.StreamType;
import stroom.widget.tickbox.client.view.TickBox;

public class FeedSettingsViewImpl extends ViewImpl implements FeedSettingsView, ReadOnlyChangeHandler {
    private final Widget widget;

    @UiField
    TextArea description;
    @UiField
    TextBox classification;
    @UiField
    StringListBox dataEncoding;
    @UiField
    StringListBox contextEncoding;
    @UiField
    ItemListBox<Feed.FeedStatus> feedStatus;
    @UiField
    ItemListBox<StreamType> streamType;
    @UiField
    ItemListBox<SupportedRetentionAge> retentionAge;
    @UiField
    TickBox reference;

    @Inject
    public FeedSettingsViewImpl(final Binder binder) {
        widget = binder.createAndBindUi(this);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    @Override
    public TextArea getDescription() {
        return description;
    }

    @Override
    public TextBox getClassification() {
        return classification;
    }

    @Override
    public TickBox getReference() {
        return reference;
    }

    @Override
    public StringListBox getDataEncoding() {
        return dataEncoding;
    }

    @Override
    public StringListBox getContextEncoding() {
        return contextEncoding;
    }

    @Override
    public ItemListBox<StreamType> getStreamType() {
        return streamType;
    }

    @Override
    public ItemListBox<SupportedRetentionAge> getRetentionAge() {
        return retentionAge;
    }

    @Override
    public ItemListBox<FeedStatus> getFeedStatus() {
        return feedStatus;
    }

    @Override
    public void onReadOnly(final boolean readOnly) {
        description.setEnabled(!readOnly);
        classification.setEnabled(!readOnly);
        dataEncoding.setEnabled(!readOnly);
        contextEncoding.setEnabled(!readOnly);
        streamType.setEnabled(!readOnly);
        feedStatus.setEnabled(!readOnly);
        retentionAge.setEnabled(!readOnly);
        reference.setEnabled(!readOnly);
    }

    public interface Binder extends UiBinder<Widget, FeedSettingsViewImpl> {
    }
}
