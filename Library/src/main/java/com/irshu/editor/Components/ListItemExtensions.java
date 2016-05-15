package com.irshu.editor.Components;
import android.app.Activity;
import android.content.Context;
import android.support.v4.media.VolumeProviderCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.irshu.editor.BaseClass;
import com.irshu.editor.R;
import com.irshu.editor.models.EditorControl;
import com.irshu.editor.models.EditorType;
import com.irshu.editor.models.RenderType;

import org.jsoup.helper.StringUtil;

/**
 * Created by mkallingal on 5/1/2016.
 */
public class ListItemExtensions {
    private Context _Context;
    BaseClass _Base;
    public ListItemExtensions(BaseClass baseClass){
        this._Base= baseClass;
        this._Context= baseClass._Context;
    }

    public TableLayout insertList(int Index, boolean isOrdered, String text){
        TableLayout _table=CreateTable();
        _Base._ParentView.addView(_table, Index);
        _table.setTag(_Base.CreateTag(isOrdered ? EditorType.ol : EditorType.ul));
        AddListItem(_table, isOrdered, text);
        if(_Base._RenderType== RenderType.Editor) {

        }
        return _table;
    }

    public TableLayout CreateTable(){
        TableLayout table = new TableLayout(_Context);
        table.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        table.setPadding(30, 10, 10, 10);
        return table;
    }

    public View AddListItem(TableLayout layout, boolean isOrdered, String text){
        final View childLayout = ((Activity) _Context).getLayoutInflater().inflate(R.layout.tmpl_unordered_list_item, null);
        final CustomEditText editText= (CustomEditText) childLayout.findViewById(R.id.txtText);
        editText.setTextColor(_Base._Resources.getColor(R.color.darkertext));
        editText.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.0f, _Base._Resources.getDisplayMetrics()), 1.0f);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setTag(_Base.CreateTag(isOrdered ? EditorType.OL_LI : EditorType.UL_LI));
        childLayout.setTag(_Base.CreateTag(isOrdered?EditorType.OL_LI : EditorType.UL_LI));
        _Base.activeView= editText;
        if(isOrdered){
            final TextView _order= (TextView) childLayout.findViewById(R.id.lblOrder);
            int count= layout.getChildCount();
            _order.setText(String.valueOf(count+1)+".");
        }

        if(!TextUtils.isEmpty(text)){
            _Base.inputExtensions.setText(editText, text);
        }



        if(_Base._RenderType==RenderType.Editor) {
            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _Base.activeView = v;
                    //   toggleToolbarProperties(v,null);
                }
            });

            editText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL) {
                        if (_Base.inputExtensions.IsEditTextNull(editText)) {
                            _Base.deleteFocusedPrevious(editText);
                        }
                    }
                    return false;
                }
            });

            editText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //                if (s.length() == 0) {
                    //                    deleteFocusedPrevious(editText);
                    //                }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String text = Html.toHtml(editText.getText());
                    if (s.length() > 0) {
                        if (s.charAt(s.length() - 1) == '\n') {
                            text = text.replaceAll("<br>", "");
                            TableRow _row = (TableRow) editText.getParent();
                            TableLayout _table = (TableLayout) _row.getParent();
                            EditorType type = _Base.GetControlType(_table);
                            if (s.length() == 0 || s.toString().equals("\n")) {
                                int index = _Base._ParentView.indexOfChild(_table);
                                _table.removeView(_row);
                                _Base.inputExtensions.InsertEditText(index + 1, "", "");
                            } else {
                                Spanned __ = Html.fromHtml(text);
                                CharSequence toReplace = _Base.inputExtensions.noTrailingwhiteLines(__);
                                editText.setText(toReplace);
                                int index = _table.indexOfChild(_row);
                                //  InsertEditText(index + 1, "");
                                AddListItem(_table, type == EditorType.ol, "");
                            }

                        }
                    }
                }
            });
        }
        else{
            editText.setClickable(false);
            editText.setCursorVisible(false);
            editText.setEnabled(false);
        }

        layout.addView(childLayout);
        if (editText.requestFocus()) {
            editText.setSelection(editText.getText().length());
        }
        return childLayout;
    }

    public void ConvertListToNormalText(TableLayout _table,int startIndex){
        int tableChildCount=_table.getChildCount();
        for(int i=startIndex;i<tableChildCount;i++) {
            View _childRow = _table.getChildAt(i);
            _table.removeView(_childRow);
            CustomEditText _EditText = ConvertListItemToNormalText(_childRow);
            int Index= _Base._ParentView.indexOfChild(_table);
            _Base.inputExtensions.InsertEditText(Index,_EditText);
            i -= 1;
            tableChildCount-=1;
        }
        //if item is the last in the table, remove the table from parent

        if(_table.getChildCount()==0){
            _Base._ParentView.removeView(_table);
        }
    }

    public void ConvertListToOrdered(TableLayout _table){
        EditorControl type= _Base.CreateTag(EditorType.ol);
        _table.setTag(type);
        for(int i=0;i<_table.getChildCount();i++){
            View _childRow = _table.getChildAt(i);
            CustomEditText editText = (CustomEditText) _childRow.findViewById(R.id.txtText);
            editText.setTag(_Base.CreateTag(EditorType.OL_LI));
            _childRow.setTag(_Base.CreateTag(EditorType.OL_LI));
            TextView _bullet= (TextView) _childRow.findViewById(R.id.lblOrder);
            _bullet.setText(String.valueOf(i + 1) + ".");
        }
    }


    public void ConvertListToUnordered(TableLayout _table){
        EditorControl type= _Base.CreateTag(EditorType.ul);
        _table.setTag(type);
        for(int i=0;i<_table.getChildCount();i++){
            View _childRow = _table.getChildAt(i);
            CustomEditText _EditText = (CustomEditText) _childRow.findViewById(R.id.txtText);
            _EditText.setTag(_Base.CreateTag(EditorType.UL_LI));
            _childRow.setTag(_Base.CreateTag(EditorType.UL_LI));
            TextView _bullet= (TextView) _childRow.findViewById(R.id.lblOrder);
            _bullet.setText("•");
        }
    }


    public CustomEditText ConvertListItemToNormalText(View row){
        CustomEditText _text= (CustomEditText) row.findViewById(R.id.txtText);
       CustomEditText editText=  _Base.inputExtensions.GetNewEditText("", _text.getText().toString());
        return editText;
    }

    public void Insertlist(boolean isOrdered){
        View ActiveView= _Base.activeView;
        EditorType currentFocus= _Base.GetControlType(ActiveView);
        if(currentFocus==EditorType.UL_LI&&!isOrdered) {
                 /* this means, current focus is on n unordered list item, since user clicked
                 on unordered list icon, loop through the parents childs and convert each list item into normal edittext
                 *
                 */
            TableRow _row = (TableRow) ActiveView.getParent();
            TableLayout _table = (TableLayout) _row.getParent();
            ConvertListToNormalText(_table,_table.indexOfChild(_row));
                    /* this means, current focus is on n unordered list item, since user clicked
                 on unordered list icon, loop through the parents childs and convert each list item into normal edittext
                 *
                 */

        }else if(currentFocus==EditorType.UL_LI&&isOrdered){

                                    /*
                    * user clicked on ordered list item. since it's an unordered list, you need to loop through each and convert each
                    * item into an ordered list.
                    * */
            TableRow _row = (TableRow) ActiveView.getParent();
            TableLayout _table = (TableLayout) _row.getParent();
            ConvertListToOrdered(_table);
                                 /*
                    * user clicked on ordered list item. since it's an unordered list, you need to loop through each and convert each
                    * item into an ordered list.
                    * */
        }
        else if(currentFocus==EditorType.OL_LI&&isOrdered) {
                /*
                *
                * this means the item was an ordered list, you need to convert the item into a normal EditText
                *
                * */
            TableRow _row = (TableRow) ActiveView.getParent();
            TableLayout _table = (TableLayout) _row.getParent();
            ConvertListToNormalText(_table,_table.indexOfChild(_row));
                /*
                *
                * this means the item was an ordered list, you need to convert the item into a normal EditText
                *
                * */
        }
        else if(currentFocus==EditorType.OL_LI&&!isOrdered){
                 /*
                *
                * this means the item was an ordered list, you need to convert the item into an unordered list
                *
                * */

            TableRow _row = (TableRow) ActiveView.getParent();
            TableLayout _table = (TableLayout) _row.getParent();
            ConvertListToUnordered(_table);
                  /*
                *
                * this means the item was an ordered list, you need to convert the item into an unordered list
                *
                * */
        }
        else if(isOrdered){
                 /*
                *
                * it's a normal edit text, convert it into an ordered list. but first check index-1, if it's ordered, should follow the order no.
                * if it's unordered, convert all of em to ordered.
                *
                * */
            int index_of_activeView= _Base._ParentView.indexOfChild(_Base.activeView);
            int Index = _Base.determineIndex(EditorType.OL_LI);
            //check if the active view has content
            View view= _Base._ParentView.getChildAt(Index);
            if(view!=null) {
                EditorType type =_Base.GetControlType(view); //if then, get the type of that view, this behaviour is so, if that line has text,
                // it needs to be converted to list item
                if(type==EditorType.INPUT){
                    String text= ((CustomEditText)view).getText().toString();  //get the text, if not null, replace it with list item
                    _Base._ParentView.removeView(view);

                    if(Index==0) {
                        insertList(Index, isOrdered, text);
                    }else if(_Base.GetControlType(_Base._ParentView.getChildAt(index_of_activeView - 1))==EditorType.ol){
                        TableLayout _table= (TableLayout) _Base._ParentView.getChildAt(index_of_activeView - 1);
                        AddListItem(_table, isOrdered, text);
                    }else{
                        insertList(Index,isOrdered,text);
                    }
                }else{
                    insertList(Index,isOrdered,"");    //otherwise
                }
            }else{
                insertList(Index,isOrdered,"");
            }


        }else{
                 /*
                *
                * it's a normal edit text, convert it into an un-ordered list
                *
                * */

            int Index = _Base.determineIndex(EditorType.UL_LI);
            //check if the active view has content
            View view= _Base._ParentView.getChildAt(Index);
            if(view!=null) {
                EditorType type =_Base.GetControlType(view); //if then, get the type of that view, this behaviour is so, if that line has text,
                // it needs to be converted to list item
                if(type==EditorType.INPUT){
                    String text= ((EditText)view).getText().toString();  //get the text, if not null, replace it with list item
                    _Base._ParentView.removeView(view);
                    insertList(Index,false,text);
                }else{
                    insertList(Index,false,"");    //otherwise
                }
            }else{
                insertList(Index,false,"");
            }
        }

    }
}
