/**
 * 
 */
package com.dpm.framework;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapta un array para ser mostrado en un ListActivity
 * @author David
 *
 */
public abstract class ListActivityArrayAdapter<T> extends BaseAdapter {

	private Context _context;
	private int _viewResourceId;
	private List<T> _itemList;
	
	public ListActivityArrayAdapter(Context context, int viewResourceId, List<T> contentList) {
		_context = context;
		_viewResourceId = viewResourceId;
		_itemList = contentList;
	}

	/**
	 * 
	 * @return Contexto asociado
	 */
	protected Context getContext(){
		return _context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView;
		
		if(convertView != null){
			itemView = convertView;
		}else{
			itemView = View.inflate(_context, _viewResourceId, null);
		}		
		T item = this.getItem(position); 
		
		return fillItemView(itemView, item);
	}

	/**
	 * Crea la visualización del elemento
	 * @param view Objeto visual que será rellenado
	 * @param item Elemento que se va a mostrar
	 * @return Vista del elemento rellenada
	 */
	abstract protected View fillItemView(View view, T item);

	@Override
	public int getCount() {
		return _itemList.size();
	}


	@Override
	public T getItem(int position) {
		return _itemList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

}
