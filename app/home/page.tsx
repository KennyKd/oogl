import Image from "next/image";

export default function Home() {
  // var ternin = document.getElementById("ts");
  // var triein = document.getElementById("trie");
  return (
    <div className=" justify-center content-center items-center h-screen"> 
      <div className="flex flex-col items-center justify-center m-auto">
        <h2 className="text-2xl font-semibold">With Trie</h2>
        <input className="rounded-l-full rounded-r-full p-4 w-80 text-gray-800" type="text" id="trie" placeholder="Start typing..." />
        {/* <div className="flex flex-row items-center justify-center m-auto mt-8">
          <h3 className="text-base">{}</h3>
        </div> */}
      </div>
      <div className="flex flex-col items-center justify-center m-auto mt-8">
        <h2 className="text-2xl font-semibold">With Ternary Search</h2>
        <input className="rounded-l-full rounded-r-full p-4 w-80 text-gray-800" type="text" id="ts" placeholder="Start typing..." />
      </div>
    </div>
  );
}
